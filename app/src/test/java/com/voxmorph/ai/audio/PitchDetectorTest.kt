package com.voxmorph.ai.audio

import com.voxmorph.ai.data.audio.PitchDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class PitchDetectorTest {

    private lateinit var pitchDetector: PitchDetector

    @Before
    fun setUp() {
        pitchDetector = PitchDetector(sampleRate = 44100)
    }

    @Test
    fun detectPitch_pureSineWave150Hz_detectsAccuratePitch() {
        val sampleRate = 44100
        val targetFreq = 150f
        val bufferSize = 2048
        val buffer = ShortArray(bufferSize)

        for (i in 0 until bufferSize) {
            val t = i.toDouble() / sampleRate
            val sample = (sin(2.0 * PI * targetFreq * t) * 20000.0).toInt().toShort()
            buffer[i] = sample
        }

        val detectedPitch = pitchDetector.detectPitch(buffer, bufferSize)

        // Pitch should be detected near 150 Hz within a small tolerance margin
        assertTrue("Detected pitch $detectedPitch should be near 150Hz", detectedPitch in 140f..160f)
    }

    @Test
    fun detectPitch_silence_returnsZero() {
        val buffer = ShortArray(2048)
        val detectedPitch = pitchDetector.detectPitch(buffer, 2048)
        assertEquals(0f, detectedPitch, 0.01f)
    }
}
