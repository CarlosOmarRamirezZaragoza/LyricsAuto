/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Repository to hold the global state of current music and lyrics.
 */
package com.frish.lyricsauto.shared.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MusicStateRepository {
    val currentLine: StateFlow<String>
    val currentSong: StateFlow<String>
    val isPlaying: StateFlow<Boolean>
    
    fun updateLine(line: String)
    fun updateSong(artist: String, title: String)
    fun updateIsPlaying(playing: Boolean)

    // Media Controls
    fun playPause()
    fun next()
    fun previous()
    
    val mediaAction: SharedFlow<MediaAction>
}

enum class MediaAction { PLAY_PAUSE, NEXT, PREVIOUS }
