/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-10-27
 * Description: Hilt module for Room database and DAO provision.
 */
package com.frish.lyricsauto.shared.di

import android.content.Context
import androidx.room.Room
import com.frish.lyricsauto.shared.data.local.LyricsDatabase
import com.frish.lyricsauto.shared.data.local.dao.LogDao
import com.frish.lyricsauto.shared.data.local.dao.LyricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLyricsDatabase(
        @ApplicationContext context: Context
    ): LyricsDatabase {
        return Room.databaseBuilder(
            context,
            LyricsDatabase::class.java,
            "lyrics_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideLyricsDao(db: LyricsDatabase): LyricsDao {
        return db.lyricsDao
    }

    @Provides
    @Singleton
    fun provideLogDao(db: LyricsDatabase): LogDao {
        return db.logDao
    }
}
