package com.voxmorph.ai.data.audio.dsp

import kotlin.math.abs

/**
 * Soft Knee Dynamic Compressor (ratio 2:1 - 4:1).
 * Smoothly levels vocal volume without squashing natural human dynamic expression.
 */
class Compressor {
    private var compressionAmount = 0f // 0.0f to 1.0f
    private var envelope = 0f

    fun setCompressionPercent(percent: Float) {
        this.compressionAmount = percent.coerceIn(0f, 1.0f)
    }

    fun process(input: ShortArray, length: Int): ShortArray {
        if (compressionAmount == 0f) return input

        val ratio = 1.0f + compressionAmount * 3.0f // 1:1 up to 4:1
        val threshold = 12000f - compressionAmount * 4000f // Dynamic threshold
        val attack = 0.01f
        val release = 0.1f

        val output = ShortArray(length)
        for (i in 0 until length) {
            val sample = input[i].toFloat()
            val absSample = abs(sample)

            // Envelope detection
            envelope += if (absSample > envelope) attack * (absSample - envelope) else release * (absSample - envelope)

            val gain = if (envelope > threshold) {
                val overDb = (envelope - threshold) / threshold
                1.0f / (1.0f + overDb * (1.0f - 1.0f / ratio))
            } else {
                1.0f
            }

            // Makeup gain correction
            val makeupGain = 1.0f + compressionAmount * 0.3f
            output[i] = (sample * gain * makeupGain).coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
