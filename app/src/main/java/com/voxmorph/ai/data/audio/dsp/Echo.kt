package com.voxmorph.ai.data.audio.dsp

/**
 * Subtle Feedback Echo (Delay <= 20%).
 * Adds subtle natural warmth delay without heavy repetitive reflections.
 */
class Echo(
    private val sampleRate: Int = 44100
) {
    private var echoAmount = 0f // 0.0f to 0.2f
    private val delayBuffer = ShortArray(sampleRate / 10) // 100ms delay line
    private var bufferIdx = 0

    fun setEchoPercent(percent: Float) {
        this.echoAmount = percent.coerceIn(0f, 0.2f)
    }

    fun process(input: ShortArray, length: Int): ShortArray {
        if (echoAmount == 0f) return input

        val output = ShortArray(length)
        for (i in 0 until length) {
            val sample = input[i].toFloat()
            val delayedSample = delayBuffer[bufferIdx].toFloat()

            delayBuffer[bufferIdx] = (sample + delayedSample * 0.3f).toInt().toShort()
            bufferIdx = (bufferIdx + 1) % delayBuffer.size

            output[i] = (sample + echoAmount * delayedSample).coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return output
    }
}
