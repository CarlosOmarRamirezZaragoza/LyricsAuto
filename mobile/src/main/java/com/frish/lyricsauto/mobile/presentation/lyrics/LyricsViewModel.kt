/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: ViewModel for the main screen.
 * Relevant Info: Manages service state and UI interactions.
 */
package com.frish.lyricsauto.mobile.presentation.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val toggleLyricsUseCase: ToggleLyricsUseCase
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = isLyricsEnabledUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            toggleLyricsUseCase(enabled)
        }
    }
}
