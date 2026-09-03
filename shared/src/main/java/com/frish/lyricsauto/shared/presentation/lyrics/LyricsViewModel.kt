/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-02
 * Description: Shared ViewModel following MVI for both Mobile and Android Auto.
 */
package com.frish.lyricsauto.shared.presentation.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frish.lyricsauto.shared.domain.manager.LyricsSyncManager
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val isLyricsEnabledUseCase: IsLyricsEnabledUseCase,
    private val toggleLyricsUseCase: ToggleLyricsUseCase,
    private val getSavedLyricsUseCase: GetSavedLyricsUseCase,
    private val deleteLyricsUseCase: DeleteLyricsUseCase,
    private val musicStateRepository: MusicStateRepository,
    private val syncManager: LyricsSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LyricsUiState())
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        combine(
            musicStateRepository.currentSong,
            musicStateRepository.currentLine,
            musicStateRepository.isPlaying,
            musicStateRepository.currentPositionMs,
            musicStateRepository.durationMs,
            musicStateRepository.fullLyrics,
            musicStateRepository.currentArtwork,
            isLyricsEnabledUseCase(),
            getSavedLyricsUseCase()
        ) { values ->
            LyricsUiState(
                currentSong = values[0] as String,
                currentLine = values[1] as String,
                isPlaying = values[2] as Boolean,
                currentPositionMs = values[3] as Long,
                durationMs = values[4] as Long,
                fullLyrics = values[5] as? com.frish.lyricsauto.shared.domain.model.Lyrics,
                currentArtwork = values[6] as? android.graphics.Bitmap,
                isEnabled = values[7] as Boolean,
                savedLyrics = values[8] as List<com.frish.lyricsauto.shared.domain.model.Lyrics>
            )
        }.onEach { _uiState.value = it }
        .launchIn(viewModelScope)
    }

    fun onIntent(intent: LyricsIntent) {
        when (intent) {
            is LyricsIntent.ToggleService -> viewModelScope.launch { toggleLyricsUseCase(intent.enabled) }
            is LyricsIntent.DeleteLyrics -> viewModelScope.launch { 
                deleteLyricsUseCase("${intent.lyrics.artistName} - ${intent.lyrics.trackName}") 
            }
            is LyricsIntent.PlayPause -> musicStateRepository.playPause()
            is LyricsIntent.Next -> musicStateRepository.next()
            is LyricsIntent.Previous -> musicStateRepository.previous()
            is LyricsIntent.Seek -> musicStateRepository.seek(intent.offsetMs)
        }
    }
}
