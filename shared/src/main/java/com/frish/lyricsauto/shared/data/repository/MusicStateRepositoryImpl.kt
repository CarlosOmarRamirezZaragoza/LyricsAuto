/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Implementation with seek support.
 */
package com.frish.lyricsauto.shared.data.repository

import android.graphics.Bitmap
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.MediaAction
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicStateRepositoryImpl @Inject constructor() : MusicStateRepository {

    private val _repositoryScope = CoroutineScope(Dispatchers.Main)

    private val _currentLine = MutableStateFlow("")
    override val currentLine: StateFlow<String> = _currentLine.asStateFlow()

    private val _currentSong = MutableStateFlow("")
    override val currentSong: StateFlow<String> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _fullLyrics = MutableStateFlow<Lyrics?>(null)
    override val fullLyrics: StateFlow<Lyrics?> = _fullLyrics.asStateFlow()

    private val _currentArtwork = MutableStateFlow<Bitmap?>(null)
    override val currentArtwork: StateFlow<Bitmap?> = _currentArtwork.asStateFlow()

    private val _mediaAction = MutableSharedFlow<MediaAction>()
    override val mediaAction: SharedFlow<MediaAction> = _mediaAction.asSharedFlow()

    override fun updateLine(line: String) {
        _currentLine.value = line
    }

    override fun updateSong(artist: String, title: String) {
        _currentSong.value = if (artist.isEmpty()) title else "$artist - $title"
    }

    override fun updateIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    override fun updatePosition(positionMs: Long) {
        _currentPositionMs.value = positionMs
    }

    override fun updateDuration(durationMs: Long) {
        _durationMs.value = durationMs
    }

    override fun updateFullLyrics(lyrics: Lyrics?) {
        _fullLyrics.value = lyrics
    }

    override fun updateArtwork(bitmap: Bitmap?) {
        _currentArtwork.value = bitmap
    }

    private var _pendingDeletion = false

    override fun scheduleDeletion() {
        _pendingDeletion = true
    }

    override fun playPause() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.PLAY_PAUSE) }
    }

    override fun next() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.NEXT) }
    }

    override fun previous() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.PREVIOUS) }
    }

    override fun seek(offsetMs: Long) {
        _repositoryScope.launch {
            if (offsetMs > 0) _mediaAction.emit(MediaAction.SEEK_FORWARD)
            else _mediaAction.emit(MediaAction.SEEK_BACKWARD)
        }
    }
}
