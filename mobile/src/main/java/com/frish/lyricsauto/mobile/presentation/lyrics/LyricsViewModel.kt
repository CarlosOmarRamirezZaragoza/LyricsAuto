/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: ViewModel for the main screen.
 * Relevant Info: Manages service state and UI interactions.
 */
package com.frish.lyricsauto.mobile.presentation.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.usecase.DeleteLyricsUseCase
import com.frish.lyricsauto.shared.domain.usecase.GetSavedLyricsUseCase
import com.frish.lyricsauto.shared.domain.usecase.IsLyricsEnabledUseCase
import com.frish.lyricsauto.shared.domain.usecase.ToggleLyricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val isLyricsEnabledUseCase: IsLyricsEnabledUseCase,
    private val toggleLyricsUseCase: ToggleLyricsUseCase,
    private val getSavedLyricsUseCase: GetSavedLyricsUseCase,
    private val deleteLyricsUseCase: DeleteLyricsUseCase
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = isLyricsEnabledUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val savedLyrics: StateFlow<List<Lyrics>> = getSavedLyricsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            toggleLyricsUseCase(enabled)
        }
    }

    fun deleteLyrics(lyrics: Lyrics) {
        viewModelScope.launch {
            // Using artist - track as spotifyId for now as per repository logic
            deleteLyricsUseCase("${lyrics.artistName} - ${lyrics.trackName}")
        }
    }
}
