package com.voxmorph.ai.domain.model

/**
 * Enumeration of 100% human-realistic voice presets.
 * Each preset represents a plausible human vocal identity without artificial distortions.
 */
enum class VoicePresetType {
    ORIGINAL,
    VOZ_RONCA,
    VOZ_PROFUNDA,
    VOZ_MADURA,
    VOZ_CALIDA,
    VOZ_FEMENINA,
    VOZ_MASCULINA,
    VOZ_SUSURRANTE,
    VOZ_LOCUTOR,
    VOZ_CUSTOM
}

data class VoicePreset(
    val type: VoicePresetType,
    val nameResId: Int,
    val descriptionResId: Int,
    val defaultParams: VoiceParams
) {
    companion object {
        val ALL_PRESETS = listOf(
            VoicePreset(
                type = VoicePresetType.ORIGINAL,
                nameResId = com.voxmorph.ai.R.string.preset_original,
                descriptionResId = com.voxmorph.ai.R.string.preset_original_desc,
                defaultParams = VoiceParams()
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_RONCA,
                nameResId = com.voxmorph.ai.R.string.preset_ronca,
                descriptionResId = com.voxmorph.ai.R.string.preset_ronca_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -2.5f,
                    formantShiftFactor = -1.2f,
                    rumblePercent = 0.35f,
                    raspPercent = 0.50f,
                    warmthPercent = 0.40f,
                    compressionPercent = 0.45f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_PROFUNDA,
                nameResId = com.voxmorph.ai.R.string.preset_profunda,
                descriptionResId = com.voxmorph.ai.R.string.preset_profunda_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -4.0f,
                    formantShiftFactor = -1.8f,
                    rumblePercent = 0.50f,
                    warmthPercent = 0.70f,
                    compressionPercent = 0.50f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_MADURA,
                nameResId = com.voxmorph.ai.R.string.preset_madura,
                descriptionResId = com.voxmorph.ai.R.string.preset_madura_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -1.8f,
                    formantShiftFactor = -0.8f,
                    rumblePercent = 0.25f,
                    warmthPercent = 0.60f,
                    compressionPercent = 0.40f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_CALIDA,
                nameResId = com.voxmorph.ai.R.string.preset_calida,
                descriptionResId = com.voxmorph.ai.R.string.preset_calida_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -0.5f,
                    formantShiftFactor = 0.0f,
                    warmthPercent = 0.85f,
                    reverbPercent = 0.12f,
                    compressionPercent = 0.30f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_FEMENINA,
                nameResId = com.voxmorph.ai.R.string.preset_femenina,
                descriptionResId = com.voxmorph.ai.R.string.preset_femenina_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = 4.5f,
                    formantShiftFactor = 2.0f,
                    warmthPercent = 0.25f,
                    breathinessPercent = 0.15f,
                    compressionPercent = 0.35f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_MASCULINA,
                nameResId = com.voxmorph.ai.R.string.preset_masculina,
                descriptionResId = com.voxmorph.ai.R.string.preset_masculina_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -5.0f,
                    formantShiftFactor = -2.2f,
                    rumblePercent = 0.45f,
                    warmthPercent = 0.65f,
                    compressionPercent = 0.50f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_SUSURRANTE,
                nameResId = com.voxmorph.ai.R.string.preset_susurrante,
                descriptionResId = com.voxmorph.ai.R.string.preset_susurrante_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = 0.0f,
                    breathinessPercent = 0.35f,
                    warmthPercent = 0.30f,
                    reverbPercent = 0.10f,
                    compressionPercent = 0.60f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_LOCUTOR,
                nameResId = com.voxmorph.ai.R.string.preset_locutor,
                descriptionResId = com.voxmorph.ai.R.string.preset_locutor_desc,
                defaultParams = VoiceParams(
                    pitchShiftSemitones = -3.0f,
                    formantShiftFactor = -1.0f,
                    rumblePercent = 0.40f,
                    warmthPercent = 0.80f,
                    compressionPercent = 0.80f,
                    reverbPercent = 0.08f
                )
            ),
            VoicePreset(
                type = VoicePresetType.VOZ_CUSTOM,
                nameResId = com.voxmorph.ai.R.string.preset_custom,
                descriptionResId = com.voxmorph.ai.R.string.preset_custom_desc,
                defaultParams = VoiceParams()
            )
        )
    }
}
