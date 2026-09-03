/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Screen to visualize app logs grouped by session.
 */
package com.frish.lyricsauto.mobile.presentation.logs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val sessionFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (selectedSession == null) "Sessions" else "Logs: ${sessionFormat.format(Date(selectedSession!!))}") 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedSession != null) viewModel.selectSession(null)
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        if (selectedSession == null) {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(sessions) { session ->
                    ListItem(
                        headlineContent = { Text("Session: ${sessionFormat.format(Date(session))}") },
                        modifier = Modifier.clickable { viewModel.selectSession(session) }
                    )
                    HorizontalDivider()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row {
                            Text(dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = log.level,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (log.level) {
                                    "ERROR" -> Color.Red
                                    "DEBUG" -> Color.Cyan
                                    else -> Color.Green
                                }
                            )
                        }
                        Text(
                            text = "[${log.tag}] ${log.message}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
