/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Robust service with bi-directional media control via shared repository.
 */
package com.frish.lyricsauto.mobile.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.util.Log
import android.view.KeyEvent
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.MediaAction
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.usecase.GetLyricsUseCase
import com.frish.lyricsauto.shared.domain.usecase.IsLyricsEnabledUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class LyricsNotificationListener : NotificationListenerService() {

    @Inject lateinit var getLyricsUseCase: GetLyricsUseCase
    @Inject lateinit var isLyricsEnabledUseCase: IsLyricsEnabledUseCase
    @Inject lateinit var musicStateRepository: MusicStateRepository

    private val _serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var _mediaSessionManager: MediaSessionManager? = null
    private var _activeController: MediaController? = null
    private var _currentLyrics: Lyrics? = null
    private val _metadataFlow = MutableSharedFlow<Pair<String, String>>(replay = 1)
    private var _syncJob: Job? = null
    private var _lastSentLine: String? = null

    companion object {
        private const val TAG = "LyricsAuto"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "!!! Service Created !!!")
        _mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        observeMetadata()
        observeMediaActions()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "!!! Listener Connected !!!")
        setupMediaListener()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeMetadata() {
        _serviceScope.launch {
            _metadataFlow
                .debounce(500L)
                .distinctUntilChanged()
                .flatMapLatest { (artist, title) ->
                    musicStateRepository.updateSong(artist, title)
                    musicStateRepository.updateLine("Buscando letra...")
                    getLyricsUseCase(artist, title)
                }
                .collect { result ->
                    result.onSuccess { lyrics ->
                        _currentLyrics = lyrics
                        _lastSentLine = null
                        startLyricsSync()
                    }
                    result.onFailure { musicStateRepository.updateLine("Letra no encontrada") }
                }
        }
    }

    private fun observeMediaActions() {
        _serviceScope.launch {
            musicStateRepository.mediaAction.collect { action ->
                val controller = _activeController ?: return@collect
                when (action) {
                    MediaAction.PLAY_PAUSE -> {
                        val state = controller.playbackState?.state
                        if (state == PlaybackState.STATE_PLAYING) {
                            controller.transportControls.pause()
                        } else {
                            controller.transportControls.play()
                        }
                    }
                    MediaAction.NEXT -> controller.transportControls.skipToNext()
                    MediaAction.PREVIOUS -> controller.transportControls.skipToPrevious()
                }
            }
        }
    }

    private fun setupMediaListener() {
        val componentName = ComponentName(this, LyricsNotificationListener::class.java)
        try {
            _mediaSessionManager?.addOnActiveSessionsChangedListener({ controllers ->
                controllers?.find { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                    ?.let { updateActiveController(it) }
            }, componentName)
            
            _mediaSessionManager?.getActiveSessions(componentName)
                ?.find { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?.let { updateActiveController(it) }
        } catch (e: Exception) {}
    }

    private fun updateActiveController(controller: MediaController) {
        val pkg = controller.packageName
        if (pkg == packageName || pkg.contains("android") || pkg.contains("phone")) return
        _activeController?.unregisterCallback(_controllerCallback)
        _activeController = controller
        _activeController?.registerCallback(_controllerCallback)
        pushMetadata(controller.metadata)
        musicStateRepository.updateIsPlaying(controller.playbackState?.state == PlaybackState.STATE_PLAYING)
    }

    private val _controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = pushMetadata(metadata)
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            musicStateRepository.updateIsPlaying(state?.state == PlaybackState.STATE_PLAYING)
            if (state?.state == PlaybackState.STATE_PLAYING) startLyricsSync()
        }
    }

    private fun pushMetadata(metadata: MediaMetadata?) {
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        _serviceScope.launch { _metadataFlow.emit(artist to title) }
    }

    private fun startLyricsSync() {
        _syncJob?.cancel()
        _syncJob = _serviceScope.launch {
            while (isActive && _currentLyrics != null) {
                val state = _activeController?.playbackState
                if (state?.state == PlaybackState.STATE_PLAYING) {
                    val pos = state.position
                    val line = _currentLyrics?.lines?.findLast { it.timestampMs <= pos }
                    if (line != null && line.text != _lastSentLine) {
                        _lastSentLine = line.text
                        musicStateRepository.updateLine(line.text)
                    }
                }
                delay(500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _serviceScope.cancel()
    }
}
