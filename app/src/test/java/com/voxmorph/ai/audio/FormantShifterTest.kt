package com.voxmorph.ai.audio

import com.voxmorph.ai.data.audio.FormantDetector
import com.voxmorph.ai.data.audio.dsp.FormantShifter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FormantShifterTest {

    private lateinit var formantShifter: FormantShifter
    private lateinit var formantDetector: FormantDetector

    @Before
    fun setUp() {
        formantShifter = FormantShifter(sampleRate = 44100)
        formantDetector = FormantDetector()
    }

    @Test
    fun formantShifter_process_returnsNonEmptyArray() {
        formantShifter.setFormantShiftFactor(1.5f)
        val input = ShortArray(2048) { 1000 }
        val output = formantShifter.process(input, 2048)

        assertNotNull(output)
        assertEquals(2048, output.size)
    }

    @Test
    fun calculateAutoShift_maleToFemale_returnsPositiveShifts() {
        val (pitchShift, formantShift) = formantDetector.calculateAutoShift(120f, targetGenderIsFemale = true)
        assertEquals(4.0f, pitchShift, 0.1f)
        assertEquals(1.8f, formantShift, 0.1f)
    }

    @Test
    fun calculateAutoShift_femaleToMale_returnsNegativeShifts() {
        val (pitchShift, formantShift) = formantDetector.calculateAutoShift(210f, targetGenderIsFemale = false)
        assertEquals(-4.5f, pitchShift, 0.1f)
        assertEquals(-2.0f, formantShift, 0.1f)
    }
}
