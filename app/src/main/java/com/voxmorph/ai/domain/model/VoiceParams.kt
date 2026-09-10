package com.voxmorph.ai.domain.model

/**
 * Encapsulates parameters for human-realistic voice transformation.
 * Hard bounds are strictly enforced in setters/clamp functions to ensure
 * transformed audio remains 100% natural, intelligible, and human.
 */
data class VoiceParams(
    val pitchShiftSemitones: Float = 0f,    // Hard bounds: -8f to +8f
    val formantShiftFactor: Float = 0f,     // Hard bounds: -3f to +3f
    val rumblePercent: Float = 0f,           // Hard bounds: 0% to 60% (0.0f..0.6f)
    val raspPercent: Float = 0f,             // Hard bounds: 0% to 70% (0.0f..0.7f)
    val warmthPercent: Float = 0f,           // Hard bounds: 0% to 100% (0.0f..1.0f)
    val breathinessPercent: Float = 0f,      // Hard bounds: 0% to 40% (0.0f..0.4f)
    val reverbPercent: Float = 0f,           // Hard bounds: 0% to 30% (0.0f..0.3f)
    val echoPercent: Float = 0f,             // Hard bounds: 0% to 20% (0.0f..0.2f)
    val compressionPercent: Float = 0f,      // Hard bounds: 0% to 100% (0.0f..1.0f)
    val isAutoModeEnabled: Boolean = false
) {
    /**
     * Enforces strictly natural human voice boundaries across all parameters.
     */
    fun sanitize(): VoiceParams = copy(
        pitchShiftSemitones = pitchShiftSemitones.coerceIn(-8.0f, 8.0f),
        formantShiftFactor = formantShiftFactor.coerceIn(-3.0f, 3.0f),
        rumblePercent = rumblePercent.coerceIn(0.0f, 0.6f),
        raspPercent = raspPercent.coerceIn(0.0f, 0.7f),
        warmthPercent = warmthPercent.coerceIn(0.0f, 1.0f),
        breathinessPercent = breathinessPercent.coerceIn(0.0f, 0.4f),
        reverbPercent = reverbPercent.coerceIn(0.0f, 0.3f),
        echoPercent = echoPercent.coerceIn(0.0f, 0.2f),
        compressionPercent = compressionPercent.coerceIn(0.0f, 1.0f)
    )
}
