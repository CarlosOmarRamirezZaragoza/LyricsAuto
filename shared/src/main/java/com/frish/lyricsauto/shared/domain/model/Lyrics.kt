/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Domain model for a song's lyrics and metadata.
 */
package com.frish.lyricsauto.shared.domain.model

data class Lyrics(
    val id: Int,
    val trackName: String,
    val artistName: String,
    val lines: List<LyricsLine>,
    val plainLyrics: String? = null
)
