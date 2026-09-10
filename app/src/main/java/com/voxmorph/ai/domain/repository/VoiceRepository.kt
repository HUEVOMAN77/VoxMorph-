package com.voxmorph.ai.domain.repository

import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.domain.model.VoicePresetType
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract managing audio processing engine state and configuration preferences.
 */
interface VoiceRepository {
    val currentParams: Flow<VoiceParams>
    val selectedPreset: Flow<VoicePresetType>
    val isEngineRunning: Flow<Boolean>
    val audioLevelFlow: Flow<Float>
    val audioWaveformFlow: Flow<FloatArray>

    suspend fun startEngine(): Result<Unit>
    suspend fun stopEngine()
    suspend fun updateParams(params: VoiceParams)
    suspend fun selectPreset(presetType: VoicePresetType)
    suspend fun setAutoMode(enabled: Boolean)

    suspend fun startRecording(): Result<Unit>
    suspend fun stopRecording(): Result<String>
    fun getRecordedFiles(): List<String>
}
