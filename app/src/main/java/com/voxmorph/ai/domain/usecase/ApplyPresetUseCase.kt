package com.voxmorph.ai.domain.usecase

import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.domain.model.VoicePreset
import com.voxmorph.ai.domain.model.VoicePresetType
import com.voxmorph.ai.domain.repository.VoiceRepository
import javax.inject.Inject

/**
 * UseCase for applying a human voice preset or switching parameters safely.
 */
class ApplyPresetUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    suspend operator fun invoke(presetType: VoicePresetType, customParams: VoiceParams? = null) {
        val preset = VoicePreset.ALL_PRESETS.find { it.type == presetType }
        val targetParams = if (presetType == VoicePresetType.VOZ_CUSTOM && customParams != null) {
            customParams.sanitize()
        } else {
            preset?.defaultParams?.sanitize() ?: VoiceParams()
        }

        voiceRepository.selectPreset(presetType)
        voiceRepository.updateParams(targetParams)
    }
}
