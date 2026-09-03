/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-11-20
 * Description: Shared renderer for lyrics visualization across Car and Mobile.
 */
package com.frish.lyricsauto.shared.util

import android.graphics.*
import com.frish.lyricsauto.shared.domain.model.Lyrics

class LyricsRenderer {

    private val _paint = Paint().apply { isAntiAlias = true }
    private val _dotPaint = Paint().apply { isAntiAlias = true; textSize = 70f }

    fun draw(
        canvas: Canvas,
        artwork: Bitmap?,
        song: String,
        lyrics: Lyrics?,
        positionMs: Long,
        durationMs: Long,
        isMirror: Boolean = false
    ) {
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        // 1. Background
        canvas.drawColor(Color.BLACK)

        if (artwork != null) {
            val paintImg = Paint().apply { isFilterBitmap = true; isAntiAlias = true }
            val canvasAspectRatio = canvasWidth / canvasHeight
            val artAspectRatio = artwork.width.toFloat() / artwork.height.toFloat()
            
            val scale: Float
            val dx: Float
            val dy: Float
            
            if (artAspectRatio > canvasAspectRatio) {
                scale = canvasHeight / artwork.height.toFloat()
                dx = (canvasWidth - artwork.width.toFloat() * scale) * 0.5f
                dy = 0f
            } else {
                scale = canvasWidth / artwork.width.toFloat()
                dx = 0f
                dy = (canvasHeight - artwork.height.toFloat() * scale) * 0.5f
            }
            
            val matrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(dx, dy)
            }
            canvas.drawBitmap(artwork, matrix, paintImg)
            canvas.drawColor(Color.argb(210, 0, 0, 0))
        }

        // --- Song Title Removed (already shown by system) ---

        val position = positionMs + 150
        val margin = canvasWidth * 0.12f
        val maxWidth = canvasWidth - (margin * 2)

        // 3. Lyrics
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
                        val wordDuration = wordWeight * lineDuration
                        val wordEndInLine = (charAccumulator.toFloat() / totalChars) * lineDuration + wordDuration
                        if (elapsedInLine >= wordEndInLine) {
                            highlightCount++
                            charAccumulator += word.length + 1
                        } else break
                    }
                }

                val baseTextSize = if (isMirror) 85f else 60f
                _paint.textSize = baseTextSize
                _paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                
                val lines = mutableListOf<List<String>>()
                var currentLineWords = mutableListOf<String>()
                var currentLineChars = 0
                val maxChars = if (isMirror) 45 else 55
                for (word in allWords) {
                    if (currentLineChars + word.length > maxChars && currentLineWords.isNotEmpty()) {
                        lines.add(currentLineWords)
                        currentLineWords = mutableListOf()
                        currentLineChars = 0
                    }
                    currentLineWords.add(word)
                    currentLineChars += word.length + 1
                }
                if (currentLineWords.isNotEmpty()) lines.add(currentLineWords)

                val lineSpacing = if (isMirror) 100f else 75f
                val totalHeight = (lines.size - 1) * lineSpacing
                // SHIFT UP: Moved lyrics block higher to create margin with progress bar
                var startY = (canvasHeight * (if (isMirror) 0.5f else 0.42f)) - (totalHeight / 2)
                
                var globalWordIdx = 0
                lines.forEachIndexed { idx, lineWords ->
                    val lineText = lineWords.joinToString(" ")
                    _paint.textSize = baseTextSize
                    _paint.textAlign = Paint.Align.CENTER
                    val mw = _paint.measureText(lineText)
                    if (mw > maxWidth) _paint.textSize = (maxWidth / mw) * baseTextSize
                    
                    val finalW = _paint.measureText(lineText)
                    var x = (canvasWidth - finalW) / 2
                    val y = startY + (idx * lineSpacing)
                    
                    _paint.textAlign = Paint.Align.LEFT
                    lineWords.forEach { word ->
                        _paint.color = if (globalWordIdx < highlightCount) Color.YELLOW else Color.WHITE
                        canvas.drawText("$word ", x, y, _paint)
                        x += _paint.measureText("$word ")
                        globalWordIdx++
                    }
                }

                nextLine?.let {
                    _paint.textAlign = Paint.Align.CENTER
                    _paint.textSize = if (isMirror) 40f else 30f
                    _paint.color = Color.LTGRAY
                    _paint.typeface = Typeface.DEFAULT
                    // Fixed margin: separation from the progress bar
                    val nextY = startY + (lines.size * lineSpacing) + 5f
                    canvas.drawText(it.text.take(50), canvasWidth / 2, nextY, _paint)
                }
            } else {
                _drawLoadingDots(canvas)
            }
        } else {
            _drawLoadingDots(canvas)
        }

        // 4. Progress Bar (Bottom)
        if (durationMs > 0) {
            // SHIFT DOWN: Progress bar moved lower (92% height)
            val barY = canvasHeight * 0.92f
            val barWidth = canvasWidth * 0.65f
            val barLeft = (canvasWidth - barWidth) / 2
            val progressX = barLeft + (barWidth * (positionMs.toFloat() / durationMs)).coerceIn(0f, barWidth)

            _paint.strokeWidth = 8f
            _paint.color = Color.DKGRAY
            canvas.drawLine(barLeft, barY, barLeft + barWidth, barY, _paint)

            _paint.color = Color.YELLOW
            canvas.drawLine(barLeft, barY, progressX, barY, _paint)
            canvas.drawCircle(progressX, barY, 14f, _paint)

            // Times
            _paint.textSize = if (isMirror) 30f else 24f
            _paint.color = Color.WHITE
            _paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatTime(positionMs), barLeft - 25f, barY + 10f, _paint)
            _paint.textAlign = Paint.Align.LEFT
            canvas.drawText(formatTime(durationMs), barLeft + barWidth + 25f, barY + 10f, _paint)
        }
    }

    private fun _drawLoadingDots(canvas: Canvas) {
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val phase = (System.currentTimeMillis() / 400) % 4
        val dotWidth = _dotPaint.measureText(".")
        var dotX = centerX - (dotWidth * 1.5f)
        for (i in 0 until 3) {
            _dotPaint.color = if (i.toLong() == phase) Color.YELLOW else Color.WHITE
            canvas.drawText(".", dotX, centerY + 50f, _dotPaint)
            dotX += dotWidth
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%01d:%02d".format(min, sec)
    }
}
