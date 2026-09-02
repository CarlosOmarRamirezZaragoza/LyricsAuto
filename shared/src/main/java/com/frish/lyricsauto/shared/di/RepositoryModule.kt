/**
 * Developer: CORZ (https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
 * Date: 2024-09-01
 * Description: Hilt module for repository bindings.
 * Relevant Info: Binds implementation to interface.
 */
package com.frish.lyricsauto.shared.di

import com.frish.lyricsauto.shared.data.repository.LyricsRepositoryImpl
import com.frish.lyricsauto.shared.data.repository.MusicStateRepositoryImpl
import com.frish.lyricsauto.shared.data.repository.SettingsRepositoryImpl
import com.frish.lyricsauto.shared.domain.repository.LyricsRepository
import com.frish.lyricsauto.shared.domain.repository.MusicStateRepository
import com.frish.lyricsauto.shared.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(
        impl: LyricsRepositoryImpl
    ): LyricsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindMusicStateRepository(
        impl: MusicStateRepositoryImpl
    ): MusicStateRepository
}
