package com.voxmorph.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.domain.model.VoicePresetType
import com.voxmorph.ai.domain.repository.VoiceRepository
import com.voxmorph.ai.domain.usecase.ApplyPresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val isEngineRunning: Boolean = false,
    val selectedPreset: VoicePresetType = VoicePresetType.ORIGINAL,
    val params: VoiceParams = VoiceParams(),
    val audioLevel: Float = 0f,
    val waveform: FloatArray = FloatArray(32),
    val recordedFiles: List<String> = emptyList(),
    val isRecording: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val applyPresetUseCase: ApplyPresetUseCase
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<VoiceUiState> = combine(
        voiceRepository.isEngineRunning,
        voiceRepository.selectedPreset,
        voiceRepository.currentParams,
        voiceRepository.audioLevelFlow,
        voiceRepository.audioWaveformFlow,
        _isRecording
    ) { flowArray ->
        val running = flowArray[0] as Boolean
        val preset = flowArray[1] as VoicePresetType
        val params = flowArray[2] as VoiceParams
        val level = flowArray[3] as Float
        val waveform = flowArray[4] as FloatArray
        val recording = flowArray[5] as Boolean

        VoiceUiState(
            isEngineRunning = running,
            selectedPreset = preset,
            params = params,
            audioLevel = level,
            waveform = waveform,
            recordedFiles = voiceRepository.getRecordedFiles(),
            isRecording = recording,
            errorMessage = _errorMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VoiceUiState()
    )

    fun toggleEngine() {
        viewModelScope.launch {
            if (uiState.value.isEngineRunning) {
                voiceRepository.stopEngine()
            } else {
                val result = voiceRepository.startEngine()
                result.onFailure {
                    _errorMessage.value = it.message ?: "Failed to start audio engine"
                }
            }
        }
    }

    fun selectPreset(presetType: VoicePresetType) {
        viewModelScope.launch {
            applyPresetUseCase(presetType)
        }
    }

    fun updateParams(params: VoiceParams) {
        viewModelScope.launch {
            applyPresetUseCase(VoicePresetType.VOZ_CUSTOM, params)
        }
    }

    fun toggleAutoMode(enabled: Boolean) {
        viewModelScope.launch {
            voiceRepository.setAutoMode(enabled)
        }
    }

    fun resetToNatural() {
        viewModelScope.launch {
            applyPresetUseCase(VoicePresetType.ORIGINAL)
        }
    }

    fun toggleRecording() {
        viewModelScope.launch {
            if (_isRecording.value) {
                voiceRepository.stopRecording()
                _isRecording.value = false
            } else {
                val res = voiceRepository.startRecording()
                if (res.isSuccess) {
                    _isRecording.value = true
                } else {
                    _errorMessage.value = "Failed to start recording"
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
