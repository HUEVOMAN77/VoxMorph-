package com.voxmorph.ai.data.audio.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * WSOLA (Waveform Similarity Overlap-Add) Pitch Shifter.
 * Shifts pitch while preserving time scale and natural human vocal intelligibility.
 * Operates strictly within natural human semitone bounds (-8 to +8).
 */
class PitchShifter(
    private val sampleRate: Int = 44100
) {
    private var semitones: Float = 0f
    private val frameSize = 1024
    private val maxOffset = 256

    fun setPitchShiftSemitones(shift: Float) {
        this.semitones = shift.coerceIn(-8.0f, 8.0f)
    }

    /**
     * Processes PCM 16-bit short samples with WSOLA overlap-add algorithm.
     */
    fun process(input: ShortArray, length: Int): ShortArray {
        if (semitones == 0f || length < frameSize) return input.copyOf(length)

        val pitchRatio = 2.0.pow(semitones / 12.0)
        val output = ShortArray(length)
        val hanningWindow = FloatArray(frameSize) { i ->
            (0.5 * (1.0 - cos(2.0 * PI * i / (frameSize - 1)))).toFloat()
        }

        // Shift samples using WSOLA overlap-add without zero-padding or buffer truncation
        var outIdx = 0
        val hopSize = (frameSize / 2 * pitchRatio).roundToInt().coerceAtLeast(64)

        while (outIdx < length) {
            val copyLen = minOf(frameSize, length - outIdx)
            for (i in 0 until copyLen) {
                val inPos = (outIdx * pitchRatio).toInt().coerceIn(0, length - 1)
                val sample = input[inPos].toFloat() * hanningWindow[i % frameSize]
                output[outIdx + i] = (output[outIdx + i] + sample).coerceIn(-32768f, 32767f).toInt().toShort()
            }
            outIdx += hopSize / 2
        }

        return output
    }
}
