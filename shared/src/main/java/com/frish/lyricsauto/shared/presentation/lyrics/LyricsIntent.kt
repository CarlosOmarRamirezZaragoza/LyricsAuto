/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-02
 * Description: Intents for user actions in Lyrics screen.
 */
package com.frish.lyricsauto.shared.presentation.lyrics

import com.frish.lyricsauto.shared.domain.model.Lyrics

sealed class LyricsIntent {
    data class ToggleService(val enabled: Boolean) : LyricsIntent()
    data class DeleteLyrics(val lyrics: Lyrics) : LyricsIntent()
    object PlayPause : LyricsIntent()
    object Next : LyricsIntent()
    object Previous : LyricsIntent()
    data class Seek(val offsetMs: Long) : LyricsIntent()
}
