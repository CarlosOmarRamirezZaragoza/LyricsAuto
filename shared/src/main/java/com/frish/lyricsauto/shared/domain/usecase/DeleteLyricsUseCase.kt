/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: UseCase to delete a specific song's lyrics from the local database.
 */
package com.frish.lyricsauto.shared.domain.usecase

import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import javax.inject.Inject

class DeleteLyricsUseCase @Inject constructor(
    private val _repository: LyricsRepository
) {
    suspend operator fun invoke(spotifyId: String) {
        _repository.deleteLyrics(spotifyId)
    }
}
