/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Use case to retrieve song lyrics with reactive flow optimization.
 */
package com.frish.lyricsauto.shared.domain.usecase

import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val repository: LyricsRepository
) {
    operator fun invoke(artist: String, track: String): Flow<Result<Lyrics>> {
        // Optimization happens at the flow level to prevent redundant API calls
        return repository.getLyrics(artist, track)
            .distinctUntilChanged()
    }
}
