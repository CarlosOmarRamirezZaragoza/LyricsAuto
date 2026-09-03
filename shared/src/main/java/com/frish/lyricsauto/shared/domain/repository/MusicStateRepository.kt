/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Repository with seek and duration support.
 */
package com.frish.lyricsauto.shared.domain.repository

import android.graphics.Bitmap
import com.frish.lyricsauto.shared.domain.model.Lyrics
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MusicStateRepository {
    val currentLine: StateFlow<String>
    val currentSong: StateFlow<String>
    val isPlaying: StateFlow<Boolean>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val fullLyrics: StateFlow<Lyrics?>
    val currentArtwork: StateFlow<Bitmap?>

    fun updateLine(line: String)
    fun updateSong(artist: String, title: String)
    fun updateIsPlaying(playing: Boolean)
    fun updatePosition(positionMs: Long)
    fun updateDuration(durationMs: Long)
    fun updateFullLyrics(lyrics: Lyrics?)
    fun updateArtwork(bitmap: Bitmap?)

    fun playPause()
    fun next()
    fun previous()
    fun seek(offsetMs: Long)
    
    val mediaAction: SharedFlow<MediaAction>
}

enum class MediaAction { PLAY_PAUSE, NEXT, PREVIOUS, SEEK_FORWARD, SEEK_BACKWARD }
