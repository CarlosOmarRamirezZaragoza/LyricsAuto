/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: FTS4 Virtual Table for fast lyrics search.
 */
package com.frish.lyricsauto.shared.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = LyricsEntity::class)
@Entity(tableName = "lyrics_fts")
data class LyricsFtsEntity(
    val trackName: String,
    val artistName: String,
    val content: String
)
