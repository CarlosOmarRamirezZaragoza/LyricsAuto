/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Implementation with retry logic and metadata cleaning.
 */
package com.frish.lyricsauto.shared.data.repository

import com.frish.lyricsauto.shared.data.mapper.toDomain
import com.frish.lyricsauto.shared.data.remote.api.LyricsApi
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LyricsApi
) : LyricsRepository {

    override fun getLyrics(artist: String, track: String): Flow<Result<Lyrics>> = flow {
        val cleanArtist = cleanMetadata(artist)
        val cleanTrack = cleanMetadata(track)
        
        var retryCount = 0
        val maxRetries = 3
        var success = false

        while (retryCount < maxRetries && !success) {
            try {
                val response = api.getLyrics(cleanArtist, cleanTrack)
                emit(Result.success(response.toDomain()))
                success = true
            } catch (e: Exception) {
                if (e.message?.contains("503") == true || e.message?.contains("520") == true) {
                    retryCount++
                    delay(1000L * retryCount) // Exponential backoff
                } else {
                    emit(Result.failure(e))
                    break
                }
            }
        }
        
        if (!success && retryCount >= maxRetries) {
            emit(Result.failure(Exception("Server overloaded after retries")))
        }
    }

    private fun cleanMetadata(input: String): String {
        return input.replace(Regex("\\(.*?\\)"), "") // Remove anything in parenthesis
            .replace(Regex("\\[.*?\\]"), "")         // Remove anything in brackets
            .replace(Regex("- .*?Remaster.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("- .*?Live.*", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
