/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Room Database for the lyrics application.
 */
package com.frish.lyricsauto.shared.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frish.lyricsauto.shared.data.local.dao.LyricsDao
import com.frish.lyricsauto.shared.data.local.entity.LyricsEntity
import com.frish.lyricsauto.shared.data.local.entity.LyricsFtsEntity

@Database(
    entities = [LyricsEntity::class, LyricsFtsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LyricsDatabase : RoomDatabase() {
    abstract val lyricsDao: LyricsDao
}
