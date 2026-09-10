package com.voxmorph.ai.data.audio.dsp

import kotlin.math.PI
import kotlin.math.sin

/**
 * Vocal Roughness / Rasp Simulator.
 * Uses slow LFO amplitude/phase modulation (1-8 Hz) to simulate natural organic vocal cord
 * vibration/asperity, strictly avoiding digital distortion or ring modulators.
 */
class VocalRoughness(
    private val sampleRate: Int = 44100
) {
    private var raspPercent = 0f // 0.0f to 0.7f
    private var lfoPhase = 0.0

    fun setRaspPercent(percent: Float) {
        this.raspPercent = percent.coerceIn(0f, 0.7f)
    }

    fun process(input: ShortArray, length: Int): ShortArray {
        if (raspPercent == 0f) return input

        val lfoFreq = 4.5 // Natural vocal cord micro-vibration frequency (Hz)
        val lfoStep = 2.0 * PI * lfoFreq / sampleRate
        val depth = raspPercent * 0.25f // Soft modulation depth

        val output = ShortArray(length)
        for (i in 0 until length) {
            val sample = input[i].toFloat()
            val lfoVal = (sin(lfoPhase) * depth).toFloat()
            lfoPhase = (lfoPhase + lfoStep) % (2.0 * PI)

            val modulated = sample * (1.0f + lfoVal)
            output[i] = modulated.coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
