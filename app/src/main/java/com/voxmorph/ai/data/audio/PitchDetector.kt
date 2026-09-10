package com.voxmorph.ai.data.audio

import kotlin.math.abs

/**
 * YIN / Autocorrelation Fundamental Frequency (F0) Pitch Detector.
 * Features exponential pitch smoothing to eliminate unnatural voice jitter/vibration.
 */
class PitchDetector(
    private val sampleRate: Int = 44100
) {
    private var smoothedPitch = 0f
    private val alpha = 0.25f // Exponential smoothing weight

    /**
     * Detects fundamental pitch (F0) in Hz from 16-bit PCM buffer.
     * Human vocal range target: 70 Hz to 400 Hz.
     */
    fun detectPitch(buffer: ShortArray, length: Int): Float {
        val yinBuffer = FloatArray(length / 2)
        var runningSum = 0f

        // Difference function
        for (tau in 1 until length / 2) {
            var diff = 0f
            for (i in 0 until length / 2) {
                val delta = buffer[i].toFloat() - buffer[i + tau].toFloat()
                diff += delta * delta
            }
            yinBuffer[tau] = diff
        }

        // Cumulative mean normalized difference
        yinBuffer[0] = 1f
        for (tau in 1 until length / 2) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0f) yinBuffer[tau] * tau / runningSum else 1f
        }

        // Absolute threshold detection
        val threshold = 0.15f
        var detectedTau = -1
        for (tau in 2 until length / 2) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < length / 2 && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    // Find local minimum
                }
                detectedTau = tau
                break
            }
        }

        val rawPitch = if (detectedTau > 0) sampleRate.toFloat() / detectedTau else 0f
        val clampedPitch = if (rawPitch in 70f..400f) rawPitch else 0f

        // Exponential smoothing
        if (clampedPitch > 0f) {
            smoothedPitch = if (smoothedPitch == 0f) clampedPitch else alpha * clampedPitch + (1f - alpha) * smoothedPitch
        }

        return smoothedPitch
    }

    fun reset() {
        smoothedPitch = 0f
    }
}
