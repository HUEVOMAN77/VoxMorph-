package com.voxmorph.ai.di

import android.content.Context
import com.voxmorph.ai.data.audio.AudioEngine
import com.voxmorph.ai.data.prefs.SettingsDataStore
import com.voxmorph.ai.data.repository.VoiceRepositoryImpl
import com.voxmorph.ai.domain.repository.VoiceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideAudioEngine(
        @ApplicationContext context: Context
    ): AudioEngine {
        return AudioEngine(context)
    }

    @Provides
    @Singleton
    fun provideVoiceRepository(
        @ApplicationContext context: Context,
        audioEngine: AudioEngine,
        settingsDataStore: SettingsDataStore
    ): VoiceRepository {
        return VoiceRepositoryImpl(context, audioEngine, settingsDataStore)
    }
}
