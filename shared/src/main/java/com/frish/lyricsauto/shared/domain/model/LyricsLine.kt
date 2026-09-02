/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Model representing a single line of synced lyrics with its timestamp.
 * Relevant Info: Used for synchronized display in Android Auto notifications.
 */
package com.frish.lyricsauto.shared.domain.model

data class LyricsLine(
    val timestampMs: Long,
    val text: String
)
