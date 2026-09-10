package com.voxmorph.ai.data.audio

/**
 * Formant Detector that estimates vocal tract resonances based on detected pitch (F0)
 * and spectral power density. Categorizes vocal ranges into male (85-180 Hz) and female (165-255 Hz).
 */
class FormantDetector {

    /**
     * Calculates optimal target pitch semitone offset and formant correction factor
     * for Auto Mode based on detected input pitch.
     * Male range: 85 - 180 Hz
     * Female range: 165 - 255 Hz
     */
    fun calculateAutoShift(pitchHz: Float, targetGenderIsFemale: Boolean): Pair<Float, Float> {
        if (pitchHz < 70f || pitchHz > 380f) return Pair(0f, 0f)

        return if (targetGenderIsFemale) {
            // Male -> Female shift target
            if (pitchHz < 170f) {
                Pair(4.0f, 1.8f)
            } else {
                Pair(1.5f, 0.6f)
            }
        } else {
            // Female -> Male shift target
            if (pitchHz > 160f) {
                Pair(-4.5f, -2.0f)
            } else {
                Pair(-1.5f, -0.6f)
            }
        }
    }
}
