/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Main activity with Jetpack Compose UI mirroring Android Auto.
 */
package com.frish.lyricsauto

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frish.lyricsauto.mobile.presentation.logs.LogScreen
import com.frish.lyricsauto.mobile.presentation.logs.LogViewModel
import com.frish.lyricsauto.mobile.presentation.lyrics.LyricsListScreen
import com.frish.lyricsauto.shared.presentation.lyrics.LyricsIntent
import com.frish.lyricsauto.shared.presentation.lyrics.LyricsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: LyricsViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var currentScreenName by rememberSaveable { mutableStateOf("main") }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    primary = Color(0xFFBB86FC)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    when (currentScreenName) {
                        "logs" -> {
                            LogScreen(
                                viewModel = logViewModel,
                                onBack = { currentScreenName = "main" }
                            )
                        }
                        "mirror" -> {
                            MirrorScreen(
                                viewModel = viewModel,
                                onBack = { currentScreenName = "main" }
                            )
                        }
                        else -> {
                            MainScreen(
                                viewModel = viewModel,
                                onTitleLongClick = { currentScreenName = "logs" },
                                onMirrorClick = { currentScreenName = "mirror" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: LyricsViewModel,
    onTitleLongClick: () -> Unit,
    onMirrorClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    var isNotificationGranted by remember { mutableStateOf(false) }
    var clickCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            isNotificationGranted = isNotificationServiceEnabled(context)
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFBB86FC),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    clickCount++
                    if (clickCount >= 5) {
                        onTitleLongClick()
                        clickCount = 0
                    }
                }
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_grant_permission))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isNotificationGranted) 
                stringResource(R.string.status_permission_granted) 
            else 
                stringResource(R.string.status_permission_denied),
            color = if (isNotificationGranted) Color.Green else Color.Red,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.sw_enable_lyrics),
                fontSize = 16.sp,
                color = Color.White
            )
            Switch(
                modifier = Modifier.width(12.dp),
                checked = state.isEnabled,
                onCheckedChange = { viewModel.onIntent(LyricsIntent.ToggleService(it)) }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.Gray)

        Box(modifier = Modifier.weight(1f)) {
            LyricsListScreen(
                lyricsList = state.savedLyrics,
                onDelete = { viewModel.onIntent(LyricsIntent.DeleteLyrics(it)) },
                currentSong = state.currentSong,
                currentLine = state.currentLine,
                onMirrorClick = onMirrorClick
            )
        }
    }
}

@Composable
fun MirrorScreen(
    viewModel: LyricsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    BackHandler { onBack() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Background Artwork
        state.currentArtwork?.let { artwork ->
            Image(
                bitmap = artwork.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
        }
        
        // 2. Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = state.currentSong,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Lyrics Logic (Karaoke effect in Compose)
            val lyrics = state.fullLyrics
            val position = state.currentPositionMs + 150
            
            if (lyrics != null && lyrics.lines.isNotEmpty()) {
                val currentLineIndex = lyrics.lines.indexOfLast { it.timestampMs <= position }
                val currentLineObj = if (currentLineIndex != -1) lyrics.lines[currentLineIndex] else null
                val nextLineObj = if (currentLineIndex != -1 && currentLineIndex + 1 < lyrics.lines.size) {
                    lyrics.lines[currentLineIndex + 1]
                } else null

                if (currentLineObj != null) {
                    val words = currentLineObj.text.split(" ")
                    val lineStart = currentLineObj.timestampMs
                    val lineEnd = nextLineObj?.timestampMs ?: (lineStart + 5000)
                    val lineDuration = lineEnd - lineStart
                    
                    val elapsedInLine = (position - lineStart).coerceAtLeast(0)
                    var highlightCount = 0
                    if (words.isNotEmpty() && lineDuration > 0) {
                        var charAccumulator = 0
                        val totalChars = currentLineObj.text.length
                        for (word in words) {
                            val wordWeight = word.length.toFloat() / totalChars
                            val wordEndInLine = (charAccumulator.toFloat() / totalChars) * lineDuration + (wordWeight * lineDuration)
                            if (elapsedInLine >= wordEndInLine) {
                                highlightCount++
                                charAccumulator += word.length + 1
                            } else break
                        }
                    }

                    Text(
                        text = buildAnnotatedString {
                            words.forEachIndexed { index, word ->
                                withStyle(style = SpanStyle(color = if (index < highlightCount) Color.Yellow else Color.White)) {
                                    append("$word ")
                                }
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    nextLineObj?.let {
                        Text(
                            text = it.text,
                            color = Color.Gray,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                } else {
                    CircularProgressIndicator(color = Color.Yellow)
                }
            } else {
                CircularProgressIndicator(color = Color.Yellow)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Progress Bar & Controls
            if (state.durationMs > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = { state.currentPositionMs.toFloat() / state.durationMs },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color.Yellow,
                        trackColor = Color.DarkGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(state.currentPositionMs), color = Color.White, fontSize = 12.sp)
                        Text(formatTime(state.durationMs), color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                IconButton(onClick = { viewModel.onIntent(LyricsIntent.Previous) }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
                IconButton(onClick = { viewModel.onIntent(LyricsIntent.PlayPause) }) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                IconButton(onClick = { viewModel.onIntent(LyricsIntent.Next) }) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%01d:%02d".format(minutes, seconds)
}

private fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}
