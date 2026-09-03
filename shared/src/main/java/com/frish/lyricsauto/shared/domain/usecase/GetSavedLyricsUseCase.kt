/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: UseCase to retrieve all saved lyrics from the local database.
 */
package com.frish.lyricsauto.shared.domain.usecase

import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedLyricsUseCase @Inject constructor(
    private val _repository: LyricsRepository
) {
    operator fun invoke(): Flow<List<Lyrics>> {
        return _repository.getSavedLyrics()
    }
}
