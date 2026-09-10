package com.voxmorph.ai.data.audio.dsp

import kotlin.math.abs

/**
 * De-Esser to tame sibilance frequencies (5kHz - 8kHz) when boosting high frequencies or pitch.
 */
class DeEsser {
    private var bandpassState = 0f
    private var envelope = 0f

    fun process(input: ShortArray, length: Int): ShortArray {
        val output = ShortArray(length)
        val threshold = 18000f

        for (i in 0 until length) {
            val sample = input[i].toFloat()
            // Highpass filter approximation around 6kHz
            val highFreq = sample - bandpassState
            bandpassState = bandpassState + 0.3f * highFreq

            val absHigh = abs(highFreq)
            envelope = 0.95f * envelope + 0.05f * absHigh

            val attenuation = if (envelope > threshold) {
                1.0f - 0.4f * ((envelope - threshold) / (32767f - threshold)).coerceIn(0f, 1f)
            } else {
                1.0f
            }

            output[i] = (sample * attenuation).coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
