package com.voxmorph.ai.data.audio.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Parametric Equalizer utilizing biquad filters:
 * - LowShelf (Gravedad/Rumble) for deep warmth.
 * - Peaking (Medios/Warmth) for human vocal presence and warmth.
 * - HighShelf (Agudos/Breathiness) for smooth natural air.
 */
class ParametricEQ(
    private val sampleRate: Int = 44100
) {
    private var lowShelfDb = 0f
    private var warmthDb = 0f
    private var breathDb = 0f

    // LowShelf State
    private var lsX1 = 0f; private var lsX2 = 0f; private var lsY1 = 0f; private var lsY2 = 0f
    // Peaking Warmth State
    private var pkX1 = 0f; private var pkX2 = 0f; private var pkY1 = 0f; private var pkY2 = 0f
    // HighShelf State
    private var hsX1 = 0f; private var hsX2 = 0f; private var hsY1 = 0f; private var hsY2 = 0f

    fun setParams(rumblePercent: Float, warmthPercent: Float, breathinessPercent: Float) {
        // Map percentages to subtle dB gains
        this.lowShelfDb = (rumblePercent.coerceIn(0f, 0.6f) * 10f) // 0 to +6dB
        this.warmthDb = (warmthPercent.coerceIn(0f, 1.0f) * 6f)    // 0 to +6dB
        this.breathDb = (breathinessPercent.coerceIn(0f, 0.4f) * 8f) // 0 to +3.2dB
    }

    fun process(input: ShortArray, length: Int): ShortArray {
        if (lowShelfDb == 0f && warmthDb == 0f && breathDb == 0f) return input

        val output = ShortArray(length)
        for (i in 0 until length) {
            var sample = input[i].toFloat()

            // 1. LowShelf Filter (120 Hz)
            if (lowShelfDb > 0f) {
                sample = processLowShelf(sample, 120f, lowShelfDb)
            }
            // 2. Peaking Mid Filter (400 Hz - warmth)
            if (warmthDb > 0f) {
                sample = processPeaking(sample, 400f, warmthDb)
            }
            // 3. HighShelf Air Filter (7000 Hz)
            if (breathDb > 0f) {
                sample = processHighShelf(sample, 7000f, breathDb)
            }

            output[i] = sample.coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }

    private fun processLowShelf(x: Float, freq: Float, gainDb: Float): Float {
        val a = Math.pow(10.0, gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val alpha = sin(w0) / 2.0 * Math.sqrt(2.0)

        val b0 = a * ((a + 1) - (a - 1) * cos(w0) + 2 * Math.sqrt(a) * alpha)
        val b1 = 2 * a * ((a - 1) - (a + 1) * cos(w0))
        val b2 = a * ((a + 1) - (a - 1) * cos(w0) - 2 * Math.sqrt(a) * alpha)
        val a0 = (a + 1) + (a - 1) * cos(w0) + 2 * Math.sqrt(a) * alpha
        val a1 = -2 * ((a - 1) + (a + 1) * cos(w0))
        val a2 = (a + 1) + (a - 1) * cos(w0) - 2 * Math.sqrt(a) * alpha

        val y = (b0/a0)*x + (b1/a0)*lsX1 + (b2/a0)*lsX2 - (a1/a0)*lsY1 - (a2/a0)*lsY2
        lsX2 = lsX1; lsX1 = x; lsY2 = lsY1; lsY1 = y.toFloat()
        return y.toFloat()
    }

    private fun processPeaking(x: Float, freq: Float, gainDb: Float): Float {
        val a = Math.pow(10.0, gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val alpha = sin(w0) / (2.0 * 1.0) // Q=1.0

        val b0 = 1.0 + alpha * a
        val b1 = -2.0 * cos(w0)
        val b2 = 1.0 - alpha * a
        val a0 = 1.0 + alpha / a
        val a1 = -2.0 * cos(w0)
        val a2 = 1.0 - alpha / a

        val y = (b0/a0)*x + (b1/a0)*pkX1 + (b2/a0)*pkX2 - (a1/a0)*pkY1 - (a2/a0)*pkY2
        pkX2 = pkX1; pkX1 = x; pkY2 = pkY1; pkY1 = y.toFloat()
        return y.toFloat()
    }

    private fun processHighShelf(x: Float, freq: Float, gainDb: Float): Float {
        val a = Math.pow(10.0, gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val alpha = sin(w0) / 2.0 * Math.sqrt(2.0)

        val b0 = a * ((a + 1) + (a - 1) * cos(w0) + 2 * Math.sqrt(a) * alpha)
        val b1 = -2 * a * ((a - 1) + (a + 1) * cos(w0))
        val b2 = a * ((a + 1) + (a - 1) * cos(w0) - 2 * Math.sqrt(a) * alpha)
        val a0 = (a + 1) - (a - 1) * cos(w0) + 2 * Math.sqrt(a) * alpha
        val a1 = 2 * ((a - 1) - (a + 1) * cos(w0))
        val a2 = (a + 1) - (a - 1) * cos(w0) - 2 * Math.sqrt(a) * alpha

        val y = (b0/a0)*x + (b1/a0)*hsX1 + (b2/a0)*hsX2 - (a1/a0)*hsY1 - (a2/a0)*hsY2
        hsX2 = hsX1; hsX1 = x; hsY2 = hsY1; hsY1 = y.toFloat()
        return y.toFloat()
    }
}
