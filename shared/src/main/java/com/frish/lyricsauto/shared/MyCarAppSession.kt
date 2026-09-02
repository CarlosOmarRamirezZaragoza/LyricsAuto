/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Car App Session that injects dependencies.
 */
package com.frish.lyricsauto.shared

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import javax.inject.Inject

class MyCarAppSession(
    private val musicStateRepository: MusicStateRepository
) : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MyCarAppScreen(carContext, musicStateRepository)
    }
}
