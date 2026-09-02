/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Use case to toggle the lyrics service state.
 */
package com.frish.lyricsauto.shared.domain.usecase

import com.frish.lyricsauto.shared.domain.repository.SettingsRepository
import javax.inject.Inject

class ToggleLyricsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setServiceEnabled(enabled)
    }
}
