/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Service with immediate metadata updates and seeking capabilities.
 */
package com.frish.lyricsauto.mobile.service

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import com.frish.lyricsauto.shared.domain.manager.LyricsSyncManager
import com.frish.lyricsauto.shared.domain.repository.MediaAction
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LyricsNotificationListener : NotificationListenerService() {

    @Inject lateinit var syncManager: LyricsSyncManager
    @Inject lateinit var musicStateRepository: MusicStateRepository
    @Inject lateinit var logger: AppLogger

    private val _serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var _mediaSessionManager: MediaSessionManager? = null
    @Volatile
    private var _activeController: MediaController? = null
    @Volatile
    private var _syncJob: Job? = null

    companion object {
        private const val TAG = "LyricsAuto"
    }

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "!!! Service Created !!!")
        _mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        observeMediaActions()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        logger.i(TAG, "!!! Listener Connected !!!")
        setupMediaListener()
    }

    private fun observeMediaActions() {
        _serviceScope.launch {
            musicStateRepository.mediaAction.collect { action ->
                val controller = _activeController ?: return@collect
                val currentPos = controller.playbackState?.position ?: 0L
                when (action) {
                    MediaAction.PLAY_PAUSE -> {
                        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) controller.transportControls.pause()
                        else controller.transportControls.play()
                    }
                    MediaAction.NEXT -> controller.transportControls.skipToNext()
                    MediaAction.PREVIOUS -> controller.transportControls.skipToPrevious()
                    MediaAction.SEEK_FORWARD -> controller.transportControls.seekTo(currentPos + 10000)
                    MediaAction.SEEK_BACKWARD -> controller.transportControls.seekTo(currentPos - 10000)
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
        logger.i(TAG, "Updating active controller: $pkg")
        if (pkg == packageName || pkg.contains("android") || pkg.contains("phone")) return
        _activeController?.unregisterCallback(_controllerCallback)
        _activeController = controller
        _activeController?.registerCallback(_controllerCallback)
        handleMetadataChange(controller.metadata)
        syncManager.updatePlaybackState(
            controller.playbackState?.state == PlaybackState.STATE_PLAYING,
            controller.playbackState?.position ?: 0L
        )
    }

    private val _controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = handleMetadataChange(metadata)
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            syncManager.updatePlaybackState(
                state?.state == PlaybackState.STATE_PLAYING,
                state?.position ?: 0L
            )
            if (state?.state == PlaybackState.STATE_PLAYING) startLyricsSync()
        }
    }

    private fun handleMetadataChange(metadata: MediaMetadata?) {
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        
        syncManager.updateMetadata(artist, title, duration, artwork)
    }

    private fun startLyricsSync() {
        _syncJob?.cancel()
        _syncJob = _serviceScope.launch {
            while (isActive) {
                val state = _activeController?.playbackState
                if (state?.state == PlaybackState.STATE_PLAYING) {
                    syncManager.updatePlaybackState(true, state.position)
                }
                delay(150)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _serviceScope.cancel()
    }
}
