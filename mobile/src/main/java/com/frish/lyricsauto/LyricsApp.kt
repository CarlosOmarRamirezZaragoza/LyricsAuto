/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Application class for Hilt initialization and crash logging.
 */
package com.frish.lyricsauto

import android.app.Application
import com.frish.lyricsauto.shared.util.AppLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class LyricsApp : Application() {
    @Inject lateinit var logger: AppLogger

    override fun onCreate() {
        super.onCreate()
        logger.startSystemLogCapture()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runBlocking(Dispatchers.IO) {
                logger.e("CRASH_FATAL", "Thread: ${thread.name}", throwable)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
        logger.i("App", "Application Started")
    }
}
