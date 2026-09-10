package be.tarsos.dsp.core

/**
 * Placeholder stub interface for TarsosDSP Core in offline/standalone builds.
 */
interface AudioProcessor {
    fun process(audioEvent: Any): Boolean
    fun processingFinished()
}
