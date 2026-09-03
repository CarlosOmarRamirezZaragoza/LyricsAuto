/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Mappers between Remote DTOs, Domain models and Data entities.
 */
package com.frish.lyricsauto.shared.data.mapper

import com.frish.lyricsauto.shared.data.local.entity.LyricsEntity
import com.frish.lyricsauto.shared.data.remote.dto.LyricsResponseDto
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.model.LyricsLine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun LyricsResponseDto.toDomain(): Lyrics {
    val lines = syncedLyrics?.lineSequence()?.mapNotNull { line ->
        val match = Regex("\\[(\\d+):(\\d+\\.\\d+)\\](.*)").find(line)
        if (match != null) {
            val mins = match.groupValues[1].toLong()
            val secs = match.groupValues[2].toDouble()
            val text = match.groupValues[3].trim()
            val timestamp = (mins * 60 * 1000) + (secs * 1000).toLong()
            LyricsLine(timestamp, text)
        } else null
    }?.toList() ?: emptyList()

    return Lyrics(
        id = id,
        trackName = trackName,
        artistName = artistName,
        lines = lines,
        plainLyrics = plainLyrics
    )
}

fun LyricsEntity.toLyrics(): Lyrics {
    val type = object : TypeToken<List<LyricsLine>>() {}.type
    val lines: List<LyricsLine> = Gson().fromJson(linesJson, type)
    return Lyrics(
        id = id,
        trackName = trackName,
        artistName = artistName,
        lines = lines,
        plainLyrics = content
    )
}

fun Lyrics.toEntity(spotifyId: String, dataSize: Long): LyricsEntity {
    val linesJson = Gson().toJson(lines)
    return LyricsEntity(
        spotifyId = spotifyId,
        trackName = trackName,
        artistName = artistName,
        content = plainLyrics ?: "",
        linesJson = linesJson,
        timestamp = System.currentTimeMillis(),
        dataSize = dataSize
    )
}
