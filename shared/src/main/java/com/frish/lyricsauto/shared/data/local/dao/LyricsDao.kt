/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Data Access Object for lyrics.
 * Relevant Info: Uses FTS for fast searching and Flow for async observation.
 */
package com.frish.lyricsauto.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frish.lyricsauto.shared.data.local.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics ORDER BY timestamp DESC")
    fun getAllLyrics(): Flow<List<LyricsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE spotifyId = :spotifyId")
    suspend fun deleteBySpotifyId(spotifyId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM lyrics WHERE spotifyId = :spotifyId)")
    suspend fun exists(spotifyId: String): Boolean

    @Query("SELECT * FROM lyrics WHERE spotifyId = :spotifyId")
    suspend fun getBySpotifyId(spotifyId: String): LyricsEntity?

    @Query("SELECT COUNT(*) FROM lyrics")
    suspend fun getCount(): Int

    @Query("SELECT SUM(dataSize) FROM lyrics")
    suspend fun getTotalSize(): Long?

    @Query("DELETE FROM lyrics WHERE timestamp = (SELECT MIN(timestamp) FROM lyrics)")
    suspend fun deleteOldest()

    @Query("""
        SELECT * FROM lyrics 
        JOIN lyrics_fts ON lyrics.rowid = lyrics_fts.rowid 
        WHERE lyrics_fts MATCH :query
    """)
    fun searchLyrics(query: String): Flow<List<LyricsEntity>>
}
