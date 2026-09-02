/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Mapper to transform DTOs to Domain models.
 */
package com.frish.lyricsauto.shared.data.mapper

import com.frish.lyricsauto.shared.data.remote.dto.LyricsResponseDto
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.model.LyricsLine

fun LyricsResponseDto.toDomain(): Lyrics {
    return Lyrics(
        id = id,
        trackName = trackName,
        artistName = artistName,
        lines = syncedLyrics?.let { parseLrc(it) } ?: emptyList(),
        plainLyrics = plainLyrics
    )
}

private fun parseLrc(lrcContent: String): List<LyricsLine> {
    val lines = mutableListOf<LyricsLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)")
    
    lrcContent.lineSequence().forEach { line ->
        val match = regex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val ms = match.groupValues[3].toLong() * 10
            val timestamp = (min * 60 * 1000) + (sec * 1000) + ms
            val text = match.groupValues[4].trim()
            if (text.isNotEmpty()) {
                lines.add(LyricsLine(timestamp, text))
            }
        }
    }
    return lines
}
