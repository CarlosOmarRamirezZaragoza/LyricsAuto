/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-11-20
 * Description: Mirror activity that displays the same content as Android Auto.
 */
package com.frish.lyricsauto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.util.AppLogger
import com.frish.lyricsauto.shared.util.LyricsRenderer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MirrorActivity : ComponentActivity() {

    @Inject lateinit var musicStateRepository: MusicStateRepository
    @Inject lateinit var logger: AppLogger
    
    private lateinit var _renderer: LyricsRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _renderer = LyricsRenderer()
        enableEdgeToEdge()
        
        setContent {
            val song by musicStateRepository.currentSong.collectAsStateWithLifecycle()
            val artwork by musicStateRepository.currentArtwork.collectAsStateWithLifecycle()
            val pos by musicStateRepository.currentPositionMs.collectAsStateWithLifecycle()
            val duration by musicStateRepository.durationMs.collectAsStateWithLifecycle()
            val lyrics by musicStateRepository.fullLyrics.collectAsStateWithLifecycle()
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    _renderer.draw(
                        canvas.nativeCanvas,
                        artwork,
                        song,
                        lyrics,
                        pos,
                        duration,
                        isMirror = true
                    )
                }
            }
            
            // Ensure UI updates for smooth progress bar and animations
            LaunchedEffect(pos) {
                // Re-composition triggered by pos change
            }
        }
    }
}
