/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Screen with media controls optimized for Dashboard constraints.
 */
package com.frish.lyricsauto.shared

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyCarAppScreen(
    carContext: CarContext,
    private val musicStateRepository: MusicStateRepository
) : Screen(carContext) {

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    musicStateRepository.currentLine.collectLatest { invalidate() }
                }
                lifecycleScope.launch {
                    musicStateRepository.isPlaying.collectLatest { invalidate() }
                }
                lifecycleScope.launch {
                    musicStateRepository.currentSong.collectLatest { invalidate() }
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        val line = musicStateRepository.currentLine.value
        val song = musicStateRepository.currentSong.value
        val isPlaying = musicStateRepository.isPlaying.value

        Log.d("LyricsAuto", "Updating Template: $line")

        // Lyrics row
        val lyricsRow = Row.Builder()
            .setTitle(if (line.isEmpty()) "Esperando música..." else line)
            .addText(if (song.isEmpty()) "Abre una app de música" else song)
            .build()

        // Action Strip (Top) - Previous Button
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_previous)).build())
                    .setOnClickListener { musicStateRepository.previous() }
                    .build()
            )
            .build()

        // Pane Actions (Bottom) - Play/Pause and Next (Max 2 allowed)
        val playPauseAction = Action.Builder()
            .setIcon(CarIcon.Builder(IconCompat.createWithResource(
                carContext, 
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )).build())
            .setOnClickListener { musicStateRepository.playPause() }
            .build()

        val nextAction = Action.Builder()
            .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_next)).build())
            .setOnClickListener { musicStateRepository.next() }
            .build()

        val pane = Pane.Builder()
            .addRow(lyricsRow)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Lyrics Auto")
            .setHeaderAction(Action.BACK) // Fixed: Prevents crash by not trying to open phone activity
            .setActionStrip(actionStrip)
            .build()
    }
}
