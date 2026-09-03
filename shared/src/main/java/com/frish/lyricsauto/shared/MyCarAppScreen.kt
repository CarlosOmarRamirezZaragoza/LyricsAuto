/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Screen with progress bar, artwork background, and animated loading dots.
 */
package com.frish.lyricsauto.shared

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.frish.lyricsauto.shared.R
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.usecase.DeleteLyricsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MyCarAppScreen(
    carContext: CarContext,
    private val musicStateRepository: MusicStateRepository,
    private val deleteLyricsUseCase: DeleteLyricsUseCase
) : Screen(carContext), SurfaceCallback {

    private var _surfaceContainer: SurfaceContainer? = null

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(this)
        
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    musicStateRepository.currentPositionMs.collectLatest { 
                        _surfaceContainer?.let { render(it) }
                    }
                }
                lifecycleScope.launch {
                    musicStateRepository.currentArtwork.collectLatest { 
                        _surfaceContainer?.let { render(it) }
                        invalidate()
                    }
                }
                lifecycleScope.launch {
                    musicStateRepository.currentSong.collectLatest { 
                        invalidate()
                    }
                }
                lifecycleScope.launch {
                    musicStateRepository.isPlaying.collectLatest { invalidate() }
                }
                // Loading animation loop
                lifecycleScope.launch {
                    while (isActive) {
                        val pos = musicStateRepository.currentPositionMs.value
                        val lyrics = musicStateRepository.fullLyrics.value
                        val hasNoLine = lyrics?.lines?.indexOfLast { it.timestampMs <= pos } == -1
                        if (lyrics == null || hasNoLine) {
                            _surfaceContainer?.let { render(it) }
                        }
                        delay(400)
                    }
                }
            }
        })
    }

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        _surfaceContainer = surfaceContainer
        render(surfaceContainer)
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        _surfaceContainer = null
    }

    private fun render(container: SurfaceContainer) {
        val surface = container.surface ?: return
        if (!surface.isValid) return
        
        try {
            val canvas = surface.lockCanvas(null) ?: return
            
            // 1. Background
            val artwork = musicStateRepository.currentArtwork.value
            if (artwork != null) {
                val paintImg = Paint().apply { isFilterBitmap = true; isAntiAlias = true }
                val canvasWidth = canvas.width.toFloat()
                val canvasHeight = canvas.height.toFloat()
                val scale = Math.max(canvasWidth / artwork.width, canvasHeight / artwork.height)
                val sw = artwork.width * scale
                val sh = artwork.height * scale
                val dst = RectF((canvasWidth - sw) / 2, (canvasHeight - sh) / 2, (canvasWidth + sw) / 2, (canvasHeight + sh) / 2)
                canvas.drawBitmap(artwork, null, dst, paintImg)
                canvas.drawColor(Color.argb(210, 0, 0, 0))
            } else {
                canvas.drawColor(Color.BLACK)
            }
            
            val lyrics = musicStateRepository.fullLyrics.value
            val position = musicStateRepository.currentPositionMs.value + 150
            val duration = musicStateRepository.durationMs.value
            val song = musicStateRepository.currentSong.value
            
            val paint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
            val margin = canvas.width * 0.15f
            val maxWidth = canvas.width - (margin * 2)

            // 2. Draw Progress Bar
            if (duration > 0) {
                val barY = canvas.height - 40f
                val barWidth = canvas.width * 0.56f
                val barLeft = (canvas.width - barWidth) / 2
                val progressX = barLeft + (barWidth * (position.toFloat() / duration))

                paint.color = Color.DKGRAY
                paint.strokeWidth = 6f
                canvas.drawLine(barLeft, barY, barLeft + barWidth, barY, paint)

                paint.color = Color.YELLOW
                canvas.drawLine(barLeft, barY, progressX, barY, paint)
                canvas.drawCircle(progressX, barY, 12f, paint)

                // Time labels
                paint.textSize = 22f
                paint.color = Color.WHITE
                val currentTime = formatTime(position)
                val totalTime = formatTime(duration)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(currentTime, barLeft - 20f, barY + 8f, paint)

                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(totalTime, barLeft + barWidth + 20f, barY + 8f, paint)
            }

            // 3. Lyrics logic
            if (lyrics != null && lyrics.lines.isNotEmpty()) {
                val currentLineIndex = lyrics.lines.indexOfLast { it.timestampMs <= position }
                val currentLine = if (currentLineIndex != -1) lyrics.lines[currentLineIndex] else null
                val nextLine = if (currentLineIndex != -1 && currentLineIndex + 1 < lyrics.lines.size) {
                    lyrics.lines[currentLineIndex + 1]
                } else null

                if (currentLine != null) {
                    val allWords = currentLine.text.split(" ")
                    val lineStart = currentLine.timestampMs
                    val lineEnd = nextLine?.timestampMs ?: (lineStart + 5000)
                    val lineDuration = lineEnd - lineStart
                    
                    val elapsedInLine = (position - lineStart).coerceAtLeast(0)
                    var highlightCount = 0
                    if (allWords.isNotEmpty() && lineDuration > 0) {
                        var charAccumulator = 0
                        val totalChars = currentLine.text.length
                        for (word in allWords) {
                            val wordWeight = word.length.toFloat() / totalChars
                            val wordEndInLine = (charAccumulator.toFloat() / totalChars) * lineDuration + (wordWeight * lineDuration)
                            if (elapsedInLine >= wordEndInLine) {
                                highlightCount++
                                charAccumulator += word.length + 1
                            } else break
                        }
                    }

                    val baseTextSize = 65f
                    val lines = mutableListOf<List<String>>()
                    var currentLineWords = mutableListOf<String>()
                    var currentLineChars = 0
                    for (word in allWords) {
                        if (currentLineChars + word.length > 55 && currentLineWords.isNotEmpty()) {
                            lines.add(currentLineWords)
                            currentLineWords = mutableListOf()
                            currentLineChars = 0
                        }
                        currentLineWords.add(word)
                        currentLineChars += word.length + 1
                    }
                    if (currentLineWords.isNotEmpty()) lines.add(currentLineWords)

                    val lineSpacing = 85f
                    val startY = (canvas.height / 2).toFloat() - ((lines.size - 1) * lineSpacing / 2)
                    var globalWordIndex = 0

                    lines.forEachIndexed { lineIdx, lineWords ->
                        val lineText = lineWords.joinToString(" ")
                        paint.textSize = baseTextSize
                        val measuredWidth = paint.measureText(lineText)
                        if (measuredWidth > maxWidth) paint.textSize = (maxWidth / measuredWidth) * baseTextSize

                        val finalWidth = paint.measureText(lineText)
                        var x = (canvas.width - finalWidth) / 2
                        val y = startY + (lineIdx * lineSpacing)

                        paint.textAlign = Paint.Align.LEFT
                        lineWords.forEach { word ->
                            paint.color = if (globalWordIndex < highlightCount) Color.YELLOW else Color.WHITE
                            canvas.drawText("$word ", x, y, paint)
                            x += paint.measureText("$word ")
                            globalWordIndex++
                        }
                    }

                    nextLine?.let {
                        paint.textAlign = Paint.Align.CENTER; paint.textSize = 30f; paint.color = Color.LTGRAY
                        val nextY = startY + (lines.size * lineSpacing) + 20f
                        canvas.drawText(it.text.take(45), (canvas.width / 2).toFloat(), nextY, paint)
                    }
                } else {
                    drawLoadingDots(canvas, paint)
                }
            } else {
                val status = if (song.isEmpty()) carContext.getString(R.string.status_inactive) else carContext.getString(R.string.status_syncing)
                drawLoadingDots(canvas, paint)
            }
            surface.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {}
    }

    private fun drawLoadingDots(canvas: android.graphics.Canvas, paint: Paint) {
        val centerX = (canvas.width / 2).toFloat()
        val centerY = (canvas.height / 2).toFloat()
        paint.color = Color.WHITE
        paint.textSize = 42f
        paint.textAlign = Paint.Align.CENTER
        
        val phase = (System.currentTimeMillis() / 400) % 4
        val dotPaint = Paint(paint).apply { textSize = 70f }
        val dotWidth = dotPaint.measureText(".")
        var dotX = centerX - (dotWidth * 1.5f)
        for (i in 0 until 3) {
            dotPaint.color = if (i.toLong() == phase) Color.YELLOW else Color.WHITE
            canvas.drawText(".", dotX, centerY + 50f, dotPaint)
            dotX += dotWidth
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%01d:%02d".format(minutes, seconds)
    }

    override fun onGetTemplate(): Template {
        val isPlaying = musicStateRepository.isPlaying.value
        val song = musicStateRepository.currentSong.value
        val actionStrip = ActionStrip.Builder()
            .addAction(Action.Builder()
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_delete)).build())
                .setOnClickListener {
                    lifecycleScope.launch {
                        val song = musicStateRepository.currentSong.value
                        if (song.isNotEmpty()) {
                            deleteLyricsUseCase(song)
                            musicStateRepository.updateFullLyrics(null)
                        }
                    }
                }.build())
            .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_previous)).build())
                .setOnClickListener { musicStateRepository.previous() }.build())
            .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)).build())
                .setOnClickListener { musicStateRepository.playPause() }.build())
            .addAction(Action.Builder().setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_media_next)).build())
                .setOnClickListener { musicStateRepository.next() }.build())
            .build()

        return MapTemplate.Builder()
            .setActionStrip(actionStrip)
            .setPane(Pane.Builder().addRow(Row.Builder().setTitle(if(song.isEmpty()) carContext.getString(R.string.app_title) else song.take(25)).build()).build())
            .build()
    }
}
