/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: DAO for application logs with session support.
 */
package com.frish.lyricsauto.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.frish.lyricsauto.shared.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("SELECT * FROM app_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLogsBySession(sessionId: Long): Flow<List<LogEntity>>

    @Query("SELECT DISTINCT sessionId FROM app_logs ORDER BY sessionId DESC")
    fun getSessions(): Flow<List<Long>>

    @Query("DELETE FROM app_logs")
    suspend fun clearLogs()
}
