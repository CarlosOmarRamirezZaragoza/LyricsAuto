/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: ViewModel for displaying logs grouped by session.
 */
package com.frish.lyricsauto.mobile.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frish.lyricsauto.shared.data.local.dao.LogDao
import com.frish.lyricsauto.shared.data.local.entity.LogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val _logDao: LogDao
) : ViewModel() {

    val sessions: StateFlow<List<Long>> = _logDao.getSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSession = MutableStateFlow<Long?>(null)
    val selectedSession = _selectedSession.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val logs: StateFlow<List<LogEntity>> = _selectedSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else _logDao.getLogsBySession(session)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSession(session: Long?) {
        _selectedSession.value = session
    }

    fun clearLogs() {
        viewModelScope.launch {
            _logDao.clearLogs()
            _selectedSession.value = null
        }
    }
}
