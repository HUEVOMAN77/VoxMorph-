package com.voxmorph.ai.data.audio.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Formant Shifter paired 1:1 with pitch shift to preserve human vocal tract geometry.
 * Prevents artificial chipmunk (unnatural high resonances) or monster effects.
 */
class FormantShifter(
    private val sampleRate: Int = 44100
) {
    private var formantFactor: Float = 0f
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    fun setFormantShiftFactor(factor: Float) {
        this.formantFactor = factor.coerceIn(-3.0f, 3.0f)
    }

    /**
     * Applies vocal tract spectral correction using biquad formant adjustment.
     */
    fun process(input: ShortArray, length: Int): ShortArray {
        if (formantFactor == 0f) return input

        // Shift spectral envelope cutoff proportionally
        val centerFreq = 2200f + formantFactor * 350f
        val clampedFreq = centerFreq.coerceIn(800f, 4000f)
        val w0 = 2.0 * PI * clampedFreq / sampleRate
        val alpha = sin(w0) / 2.0

        val b0 = (1.0 + cos(w0)) / 2.0
        val b1 = -(1.0 + cos(w0))
        val b2 = (1.0 + cos(w0)) / 2.0
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cos(w0)
        val a2 = 1.0 - alpha

        val normB0 = (b0 / a0).toFloat()
        val normB1 = (b1 / a0).toFloat()
        val normB2 = (b2 / a0).toFloat()
        val normA1 = (a1 / a0).toFloat()
        val normA2 = (a2 / a0).toFloat()

        val output = ShortArray(length)
        for (i in 0 until length) {
            val x = input[i].toFloat()
            val y = normB0 * x + normB1 * x1 + normB2 * x2 - normA1 * y1 - normA2 * y2
            x2 = x1
            x1 = x
            y2 = y1
            y1 = y
            output[i] = y.coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
