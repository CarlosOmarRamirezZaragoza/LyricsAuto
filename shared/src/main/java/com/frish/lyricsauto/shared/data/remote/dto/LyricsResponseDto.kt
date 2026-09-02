/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Data Transfer Object for LRCLIB API response.
 * Relevant Info: Maps all fields from https://lrclib.net/api/get.
 */
package com.frish.lyricsauto.shared.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LyricsResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("albumName") val albumName: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("instrumental") val instrumental: Boolean,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)
