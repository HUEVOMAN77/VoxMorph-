package com.voxmorph.ai.data.audio.dsp

/**
 * Schroeder Algorithmic Room Reverb with strictly low mix (<=30%).
 * Provides realistic acoustic space reflections without washed-out artificial hall effects.
 */
class Reverb(
    private val sampleRate: Int = 44100
) {
    private var mix = 0f // 0.0f to 0.3f max
    private val combBuffer1 = ShortArray(1116)
    private val combBuffer2 = ShortArray(1188)
    private val combBuffer3 = ShortArray(1277)
    private val combBuffer4 = ShortArray(1356)

    private var combIdx1 = 0
    private var combIdx2 = 0
    private var combIdx3 = 0
    private var combIdx4 = 0

    fun setReverbPercent(percent: Float) {
        this.mix = percent.coerceIn(0f, 0.3f)
    }

    fun process(input: ShortArray, length: Int): ShortArray {
        if (mix == 0f) return input

        val output = ShortArray(length)
        val feedback = 0.5f

        for (i in 0 until length) {
            val sample = input[i].toFloat()

            // Comb Filter 1
            val c1 = combBuffer1[combIdx1].toFloat()
            combBuffer1[combIdx1] = (sample + c1 * feedback).toInt().toShort()
            combIdx1 = (combIdx1 + 1) % combBuffer1.size

            // Comb Filter 2
            val c2 = combBuffer2[combIdx2].toFloat()
            combBuffer2[combIdx2] = (sample + c2 * feedback).toInt().toShort()
            combIdx2 = (combIdx2 + 1) % combBuffer2.size

            // Comb Filter 3
            val c3 = combBuffer3[combIdx3].toFloat()
            combBuffer3[combIdx3] = (sample + c3 * feedback).toInt().toShort()
            combIdx3 = (combIdx3 + 1) % combBuffer3.size

            // Comb Filter 4
            val c4 = combBuffer4[combIdx4].toFloat()
            combBuffer4[combIdx4] = (sample + c4 * feedback).toInt().toShort()
            combIdx4 = (combIdx4 + 1) % combBuffer4.size

            val wet = (c1 + c2 + c3 + c4) * 0.25f
            output[i] = ((1.0f - mix) * sample + mix * wet).coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
