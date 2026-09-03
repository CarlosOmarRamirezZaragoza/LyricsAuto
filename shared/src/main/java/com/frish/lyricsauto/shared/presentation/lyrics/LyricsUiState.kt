/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-02
 * Description: UI State for lyrics following MVI.
 */
package com.frish.lyricsauto.shared.presentation.lyrics

import android.graphics.Bitmap
import com.frish.lyricsauto.shared.domain.model.Lyrics

data class LyricsUiState(
    val currentSong: String = "",
    val currentLine: String = "",
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val fullLyrics: Lyrics? = null,
    val currentArtwork: Bitmap? = null,
    val isEnabled: Boolean = false,
    val savedLyrics: List<Lyrics> = emptyList()
)
