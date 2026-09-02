/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Retrofit interface for LRCLIB.
 * Relevant Info: Base URL: https://lrclib.net/
 */
package com.frish.lyricsauto.shared.data.remote.api

import com.frish.lyricsauto.shared.data.remote.dto.LyricsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface LyricsApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artist: String,
        @Query("track_name") track: String
    ): LyricsResponseDto

    companion object {
        const val BASE_URL = "https://lrclib.net/"
    }
}
