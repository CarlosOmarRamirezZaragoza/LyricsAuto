/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Repository implementation with Room persistence and capacity limits.
 * Relevant Info: Enforces 1000 items / 200MB limit. Uses FTS for search.
 */
package com.frish.lyricsauto.shared.data.repository

import com.frish.lyricsauto.shared.data.local.dao.LyricsDao
import com.frish.lyricsauto.shared.data.mapper.toDomain
import com.frish.lyricsauto.shared.data.mapper.toEntity
import com.frish.lyricsauto.shared.data.mapper.toLyrics
import com.frish.lyricsauto.shared.data.remote.api.LyricsApi
import com.frish.lyricsauto.shared.domain.model.Lyrics
import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import com.frish.lyricsauto.shared.util.AppLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LyricsApi,
    private val _lyricsDao: LyricsDao,
    private val _logger: AppLogger
) : LyricsRepository {

    override fun getLyrics(artist: String, track: String): Flow<Result<Lyrics>> = flow {
        val spotifyId = "$artist - $track"
        _logger.d("LyricsRepo", "Fetching lyrics for: $spotifyId")
        
        val cached = getLyricsById(spotifyId)
        if (cached != null) {
            _logger.i("LyricsRepo", "Found cached lyrics for: $spotifyId")
            emit(Result.success(cached))
            return@flow
        }

        val cleanArtist = _cleanMetadata(artist)
        val cleanTrack = _cleanMetadata(track)
        
        var retryCount = 0
        val maxRetries = 3
        var success = false

        while (retryCount < maxRetries && !success) {
            try {
                _logger.d("LyricsRepo", "API Call (Attempt ${retryCount + 1}): $cleanArtist - $cleanTrack")
                val response = api.getLyrics(cleanArtist, cleanTrack)
                val domain = response.toDomain()
                saveLyrics(domain, spotifyId)
                emit(Result.success(domain))
                success = true
            } catch (e: Exception) {
                _logger.e("LyricsRepo", "API Error", e)
                if (e.message?.contains("503") == true || e.message?.contains("520") == true) {
                    retryCount++
                    delay(1000L * retryCount)
                } else {
                    emit(Result.failure(e))
                    break
                }
            }
        }
        
        if (!success && retryCount >= maxRetries) {
            emit(Result.failure(Exception("Server overloaded after retries")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSavedLyrics(): Flow<List<Lyrics>> {
        return _lyricsDao.getAllLyrics().map { entities ->
            entities.map { it.toLyrics() }
        }
    }

    override suspend fun getLyricsById(spotifyId: String): Lyrics? {
        return _lyricsDao.getBySpotifyId(spotifyId)?.toLyrics()
    }

    override suspend fun saveLyrics(lyrics: Lyrics, spotifyId: String) {
        val linesJson = Gson().toJson(lyrics.lines)
        val dataSize = (lyrics.plainLyrics?.length?.toLong() ?: 0L) + linesJson.length.toLong()
        
        _logger.d("LyricsRepo", "Saving to DB: $spotifyId (Size: $dataSize bytes)")
        
        while (_lyricsDao.getCount() >= 1000 || (_lyricsDao.getTotalSize() ?: 0L) + dataSize > 200 * 1024 * 1024) {
            _logger.i("LyricsRepo", "Storage limit reached, deleting oldest entry")
            _lyricsDao.deleteOldest()
        }
        
        _lyricsDao.insertLyrics(lyrics.toEntity(spotifyId, dataSize))
    }

    override suspend fun deleteLyrics(spotifyId: String) {
        _logger.i("LyricsRepo", "Deleting lyrics: $spotifyId")
        _lyricsDao.deleteBySpotifyId(spotifyId)
    }

    override suspend fun exists(spotifyId: String): Boolean {
        return _lyricsDao.exists(spotifyId)
    }

    override fun searchLyrics(query: String): Flow<List<Lyrics>> {
        return _lyricsDao.searchLyrics("*$query*").map { entities ->
            entities.map { it.toLyrics() }
        }
    }

    private fun _cleanMetadata(input: String): String {
        return input.replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("- .*?Remaster.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("- .*?Live.*", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
