/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Interface for managing app preferences.
 */
package com.frish.lyricsauto.shared.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isServiceEnabled(): Flow<Boolean>
    suspend fun setServiceEnabled(enabled: Boolean)
}
