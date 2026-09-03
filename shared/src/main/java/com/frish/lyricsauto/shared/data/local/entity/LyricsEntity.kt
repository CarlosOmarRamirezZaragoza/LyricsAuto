/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Room Entity for storing lyrics locally.
 * Relevant Info: Includes data size and timestamp for limit enforcement.
 */
package com.frish.lyricsauto.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val id: Int = 0,
    val spotifyId: String,
    val trackName: String,
    val artistName: String,
    val content: String,
    val linesJson: String,
    val timestamp: Long,
    val dataSize: Long
)
