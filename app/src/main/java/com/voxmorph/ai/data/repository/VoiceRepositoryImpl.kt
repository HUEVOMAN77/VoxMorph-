package com.voxmorph.ai.data.repository

import android.content.Context
import android.os.Environment
import com.voxmorph.ai.data.audio.AudioEngine
import com.voxmorph.ai.data.prefs.SettingsDataStore
import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.domain.model.VoicePresetType
import com.voxmorph.ai.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepositoryImpl @Inject constructor(
    private val context: Context,
    private val audioEngine: AudioEngine,
    private val settingsDataStore: SettingsDataStore
) : VoiceRepository {

    override val currentParams: Flow<VoiceParams> = settingsDataStore.voiceParamsFlow
    override val selectedPreset: Flow<VoicePresetType> = settingsDataStore.selectedPresetFlow
    override val isEngineRunning: Flow<Boolean> = audioEngine.isRunning
    override val audioLevelFlow: Flow<Float> = audioEngine.audioLevel
    override val audioWaveformFlow: Flow<FloatArray> = audioEngine.audioWaveform

    override suspend fun startEngine(): Result<Unit> {
        val params = currentParams.first()
        audioEngine.updateParams(params)
        return audioEngine.startEngine()
    }

    override suspend fun stopEngine() {
        audioEngine.stopEngine()
    }

    override suspend fun updateParams(params: VoiceParams) {
        val sanitized = params.sanitize()
        settingsDataStore.saveVoiceParams(sanitized)
        audioEngine.updateParams(sanitized)
    }

    override suspend fun selectPreset(presetType: VoicePresetType) {
        settingsDataStore.savePreset(presetType)
    }

    override suspend fun setAutoMode(enabled: Boolean) {
        val params = currentParams.first().copy(isAutoModeEnabled = enabled)
        updateParams(params)
    }

    override suspend fun startRecording(): Result<Unit> {
        return audioEngine.startRecording()
    }

    override suspend fun stopRecording(): Result<String> {
        return audioEngine.stopRecording()
    }

    override fun getRecordedFiles(): List<String> {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "VoxMorph")
        if (!dir.exists()) return emptyList()

        return dir.listFiles { _, name -> name.endsWith(".wav") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.absolutePath } ?: emptyList()
    }
}
