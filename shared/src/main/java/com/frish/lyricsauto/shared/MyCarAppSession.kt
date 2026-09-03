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
import com.frish.lyricsauto.shared.domain.usecase.DeleteLyricsUseCase
import com.frish.lyricsauto.shared.util.AppLogger

class MyCarAppSession(
    private val musicStateRepository: MusicStateRepository,
    private val deleteLyricsUseCase: DeleteLyricsUseCase,
    private val logger: AppLogger
) : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MyCarAppScreen(carContext, musicStateRepository, deleteLyricsUseCase, logger)
    }
}
