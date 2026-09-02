/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Implementation of MusicStateRepository using StateFlow and SharedFlow.
 */
package com.frish.lyricsauto.shared.data.repository

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

    override fun playPause() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.PLAY_PAUSE) }
    }

    override fun next() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.NEXT) }
    }

    override fun previous() {
        _repositoryScope.launch { _mediaAction.emit(MediaAction.PREVIOUS) }
    }
}
