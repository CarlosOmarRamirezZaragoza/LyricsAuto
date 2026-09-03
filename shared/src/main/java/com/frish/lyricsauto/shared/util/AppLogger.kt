/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Logger with session management.
 */
package com.frish.lyricsauto.shared.util

import android.util.Log
import com.frish.lyricsauto.shared.data.local.dao.LogDao
import com.frish.lyricsauto.shared.data.local.entity.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val _logDao: LogDao
) {
    private val _scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _sessionId = System.currentTimeMillis()
    private var _isCapturing = false

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        _saveLog(tag, message, "DEBUG")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) "$message | Error: ${throwable.message}" else message
        Log.e(tag, fullMessage, throwable)
        _saveLog(tag, fullMessage, "ERROR")
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        _saveLog(tag, message, "INFO")
    }

    /**
     * Captures the process's own logcat output and saves it to the database.
     */
    fun startSystemLogCapture() {
        if (_isCapturing) return
        _isCapturing = true
        _scope.launch {
            try {
                // Clear previous logcat buffer for this run
                Runtime.getRuntime().exec("logcat -c")
                
                val process = ProcessBuilder()
                    .command("logcat", "-b", "main,system,crash", "-v", "threadtime", "*:V")
                    .start()
                
                process.inputStream.bufferedReader().use { reader ->
                    var line: String?
                    while (_isCapturing) {
                        line = reader.readLine()
                        if (line != null) {
                            if (!line.contains("dmabuf_rss")) {
                                _saveLog("SYSTEM", line, "LOGCAT")
                            }
                        } else {
                            delay(500)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AppLogger", "Logcat capture failed", e)
            }
        }
    }

    fun stopSystemLogCapture() {
        _isCapturing = false
    }

    private fun _saveLog(tag: String, message: String, level: String) {
        _scope.launch {
            _logDao.insertLog(
                LogEntity(
                    sessionId = _sessionId,
                    timestamp = System.currentTimeMillis(),
                    tag = tag,
                    message = message,
                    level = level
                )
            )
        }
    }
}
