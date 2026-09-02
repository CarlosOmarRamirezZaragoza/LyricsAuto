/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Use case to check if the lyrics service is enabled.
 */
package com.frish.lyricsauto.shared.domain.usecase

import com.frish.lyricsauto.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLyricsEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isServiceEnabled()
}
