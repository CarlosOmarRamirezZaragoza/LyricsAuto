/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-11-20
 * Description: Singleton manager to handle lyrics synchronization and state sharing.
 * Relevant Info: Centralizes logic for both Mobile and Android Auto.
 */
package com.frish.lyricsauto.shared.domain.manager

import android.graphics.Bitmap
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.usecase.GetLyricsUseCase
import com.frish.lyricsauto.shared.util.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsSyncManager @Inject constructor(
    private val _getLyricsUseCase: GetLyricsUseCase,
    private val _musicStateRepository: MusicStateRepository,
    private val _logger: AppLogger
) {
    private val _managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _metadataFlow = MutableSharedFlow<Pair<String, String>>(replay = 1)
    @Volatile
    private var _lastSentLine: String? = null

    init {
        observeMetadata()
        observePosition()
    }

    private fun observeMetadata() {
        _managerScope.launch {
            _metadataFlow
                .debounce(500L)
                .distinctUntilChanged()
                .flatMapLatest { (artist, title) ->
                    _getLyricsUseCase(artist, title)
                }
                .collect { result ->
                    result.onSuccess { lyrics ->
                        _logger.d("LyricsSyncManager", "Lyrics synced: ${lyrics.trackName}")
                        _musicStateRepository.updateFullLyrics(lyrics)
                        _lastSentLine = null
                    }
                    result.onFailure { error ->
                        _logger.e("LyricsSyncManager", "Sync failed", error)
                        _musicStateRepository.updateFullLyrics(null)
                        _musicStateRepository.updateLine("Letra no encontrada")
                    }
                }
        }
    }

    private fun observePosition() {
        _managerScope.launch {
            _musicStateRepository.currentPositionMs.collect { pos ->
                val lyrics = _musicStateRepository.fullLyrics.value ?: return@collect
                val line = lyrics.lines.findLast { it.timestampMs <= (pos + 150) }
                if (line != null && line.text != _lastSentLine) {
                    _lastSentLine = line.text
                    _musicStateRepository.updateLine(line.text)
                }
            }
        }
    }

    fun updateMetadata(artist: String, title: String, duration: Long, artwork: Bitmap?) {
        _musicStateRepository.updateSong(artist, title)
        _musicStateRepository.updateDuration(duration)
        _musicStateRepository.updateArtwork(artwork)
        _managerScope.launch { _metadataFlow.emit(artist to title) }
    }

    fun updatePlaybackState(isPlaying: Boolean, position: Long) {
        _musicStateRepository.updateIsPlaying(isPlaying)
        _musicStateRepository.updatePosition(position)
    }
}
