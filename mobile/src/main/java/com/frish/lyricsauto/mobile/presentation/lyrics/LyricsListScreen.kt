/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: UI for saved lyrics list with swipe-to-delete.
 */
package com.frish.lyricsauto.mobile.presentation.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frish.lyricsauto.R
import com.frish.lyricsauto.shared.domain.model.Lyrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsListScreen(
    lyricsList: List<Lyrics>,
    onDelete: (Lyrics) -> Unit,
    currentSong: String = "",
    currentLine: String = "",
    onMirrorClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.saved_lyrics_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        if (lyricsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.empty_lyrics_msg), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = lyricsList,
                    key = { "${it.artistName}-${it.trackName}" }
                ) { lyrics ->
                    val isCurrent = "${lyrics.artistName} - ${lyrics.trackName}".equals(currentSong, ignoreCase = true)
                    
                    _SwipeToDeleteItem(
                        lyrics = lyrics,
                        isCurrent = isCurrent,
                        currentLine = if (isCurrent) currentLine else "",
                        onDelete = { onDelete(lyrics) },
                        onMirrorClick = onMirrorClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun _SwipeToDeleteItem(
    lyrics: Lyrics,
    isCurrent: Boolean,
    currentLine: String,
    onDelete: () -> Unit,
    onMirrorClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red
            } else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.swipe_to_delete),
                    tint = Color.White
                )
            }
        },
        content = {
            ListItem(
                headlineContent = { 
                    Text(
                        text = lyrics.trackName,
                        color = if (isCurrent) Color.Yellow else Color.White,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                supportingContent = { 
                    Text(
                        text = if (isCurrent && currentLine.isNotEmpty()) currentLine else lyrics.artistName,
                        color = if (isCurrent) Color.LightGray else Color.Gray,
                        maxLines = 1
                    ) 
                },
                leadingContent = if (isCurrent) {
                    { Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Yellow) }
                } else null,
                colors = ListItemDefaults.colors(
                    containerColor = if (isCurrent) Color(0x33A020F0) else Color.Transparent
                ),
                modifier = Modifier.clickable {
                    if (isCurrent) onMirrorClick()
                }
            )
        }
    )
}
