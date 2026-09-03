/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-02
 * Description: Screen for Android Auto using shared LyricsRenderer.
 */
package com.frish.lyricsauto.shared

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.*
import androidx.car.app.navigation.model.MapTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.usecase.DeleteLyricsUseCase
import com.frish.lyricsauto.shared.util.AppLogger
import com.frish.lyricsauto.shared.util.LyricsRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MyCarAppScreen(
    carContext: CarContext,
    private val musicStateRepository: MusicStateRepository,
    private val deleteLyricsUseCase: DeleteLyricsUseCase,
    private val logger: AppLogger
) : Screen(carContext), SurfaceCallback {

    @Volatile
    private var _surfaceContainer: SurfaceContainer? = null
    private val _renderer = LyricsRenderer()
    private val _renderMutex = Mutex()

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(this)
        
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    musicStateRepository.currentPositionMs.collectLatest { 
                        _surfaceContainer?.let { render(it) }
                    }
                }
                lifecycleScope.launch {
                    musicStateRepository.currentArtwork.collectLatest { 
                        _surfaceContainer?.let { render(it) }
                        invalidate()
                    }
                }
                lifecycleScope.launch {
                    musicStateRepository.currentSong.collectLatest { invalidate() }
                }
                lifecycleScope.launch {
                    musicStateRepository.isPlaying.collectLatest { invalidate() }
                }
                
                lifecycleScope.launch {
                    while (isActive) {
                        val pos = musicStateRepository.currentPositionMs.value
                        val lyrics = musicStateRepository.fullLyrics.value
                        val hasNoLine = lyrics?.lines?.indexOfLast { it.timestampMs <= pos } == -1
                        if (lyrics == null || hasNoLine) {
                            _surfaceContainer?.let { render(it) }
                        }
                        delay(400)
                    }
                }
            }
        })
    }

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        _surfaceContainer = surfaceContainer
        render(surfaceContainer)
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        _surfaceContainer = null
    }

    private fun render(container: SurfaceContainer) {
        val surface = container.surface ?: return
        if (!surface.isValid) return
        
        lifecycleScope.launch {
            if (_renderMutex.isLocked) return@launch
            _renderMutex.withLock {
                try {
                    val canvas = surface.lockCanvas(null) ?: return@withLock
                    _renderer.draw(
                        canvas,
                        musicStateRepository.currentArtwork.value,
                        musicStateRepository.currentSong.value,
                        musicStateRepository.fullLyrics.value,
                        musicStateRepository.currentPositionMs.value,
                        musicStateRepository.durationMs.value,
                        isMirror = false
                    )
                    surface.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    // Surface might become invalid during render
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        return try {
            val isPlaying = musicStateRepository.isPlaying.value
            val song = musicStateRepository.currentSong.value
            val fullLyrics = musicStateRepository.fullLyrics.value
            
            val actionStrip = ActionStrip.Builder()
                .addAction(Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_delete)).build())
                    .setOnClickListener {
                        lifecycleScope.launch {
                            if (song.isNotEmpty()) {
                                deleteLyricsUseCase("${fullLyrics?.artistName} - ${fullLyrics?.trackName}")
                                musicStateRepository.updateFullLyrics(null)
                            }
                        }
                    }
                    .setEnabled(fullLyrics != null)
                    .build())
                .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_previous)).build())
                    .setOnClickListener { musicStateRepository.previous() }.build())
                .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)).build())
                    .setOnClickListener { musicStateRepository.playPause() }.build())
                .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_next)).build())
                    .setOnClickListener { musicStateRepository.next() }.build())
                .build()

            MapTemplate.Builder()
                .setActionStrip(actionStrip)
                .setPane(Pane.Builder()
                    .addRow(Row.Builder()
                        .setTitle(if(song.isEmpty()) carContext.getString(R.string.app_title) else song.take(25))
                        .build())
                    .build())
                .build()
        } catch (e: Exception) {
            MessageTemplate.Builder("Error loading interface").build()
        }
    }
}
