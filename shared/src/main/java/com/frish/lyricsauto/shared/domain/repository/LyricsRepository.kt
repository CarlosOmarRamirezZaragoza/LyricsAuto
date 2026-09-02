/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Interface defining the contract for fetching lyrics data.
 * Relevant Info: Implementation should handle remote API calls and local mapping.
 */
package com.frish.lyricsauto.shared.domain.repository

import com.frish.lyricsauto.shared.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    /**
     * Fetches lyrics for a specific artist and track.
     * Returns a Flow of Result containing the [Lyrics] model.
     */
    fun getLyrics(artist: String, track: String): Flow<Result<Lyrics>>
}
