package com.voxmorph.ai.data.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.voxmorph.ai.data.audio.dsp.Compressor
import com.voxmorph.ai.data.audio.dsp.DeEsser
import com.voxmorph.ai.data.audio.dsp.Echo
import com.voxmorph.ai.data.audio.dsp.FormantShifter
import com.voxmorph.ai.data.audio.dsp.ParametricEQ
import com.voxmorph.ai.data.audio.dsp.PitchShifter
import com.voxmorph.ai.data.audio.dsp.Reverb
import com.voxmorph.ai.data.audio.dsp.VocalRoughness
import com.voxmorph.ai.domain.model.VoiceParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Real-time audio streaming engine using AudioRecord + DSP processing pipeline + AudioTrack.
 * Target latency < 40ms, Buffer size = 2048 samples, PCM 16-bit Mono.
 */
@Singleton
class AudioEngine @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "AudioEngine"
        private val FALLBACK_SAMPLE_RATES = intArrayOf(44100, 48000, 16000)
        private const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = 2048
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var engineJob: Job? = null

    private var activeSampleRate = 44100
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    // DSP Pipeline components
    private var pitchShifter = PitchShifter(activeSampleRate)
    private var formantShifter = FormantShifter(activeSampleRate)
    private var parametricEQ = ParametricEQ(activeSampleRate)
    private val compressor = Compressor()
    private val deEsser = DeEsser()
    private var reverb = Reverb(activeSampleRate)
    private var echo = Echo(activeSampleRate)
    private var vocalRoughness = VocalRoughness(activeSampleRate)
    private var pitchDetector = PitchDetector(activeSampleRate)
    private val formantDetector = FormantDetector()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _audioWaveform = MutableStateFlow(FloatArray(32))
    val audioWaveform: StateFlow<FloatArray> = _audioWaveform.asStateFlow()

    private var currentParams = VoiceParams()

    // Recording State
    @Volatile
    private var isRecordingToFile = false
    private var recordingFileOutputStream: FileOutputStream? = null
    private var currentRecordingFile: File? = null
    private var totalPcmBytesWritten = 0L

    fun updateParams(params: VoiceParams) {
        val sanitized = params.sanitize()
        this.currentParams = sanitized

        pitchShifter.setPitchShiftSemitones(sanitized.pitchShiftSemitones)
        formantShifter.setFormantShiftFactor(sanitized.formantShiftFactor)
        parametricEQ.setParams(sanitized.rumblePercent, sanitized.warmthPercent, sanitized.breathinessPercent)
        compressor.setCompressionPercent(sanitized.compressionPercent)
        reverb.setReverbPercent(sanitized.reverbPercent)
        echo.setEchoPercent(sanitized.echoPercent)
        vocalRoughness.setRaspPercent(sanitized.raspPercent)
    }

    @SuppressLint("MissingPermission")
    fun startEngine(): Result<Unit> {
        if (_isRunning.value) return Result.success(Unit)

        var recordInitialized = false
        var lastError: Exception? = null

        for (sampleRate in FALLBACK_SAMPLE_RATES) {
            try {
                val minRecBufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_CONFIG_IN, AUDIO_FORMAT)
                val minTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, CHANNEL_CONFIG_OUT, AUDIO_FORMAT)

                if (minRecBufferSize <= 0 || minTrackBufferSize <= 0) continue

                val recBufferSize = maxOf(minRecBufferSize, BUFFER_SIZE * 2)
                val trackBufferSize = maxOf(minTrackBufferSize, BUFFER_SIZE * 2)

                val tempRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    CHANNEL_CONFIG_IN,
                    AUDIO_FORMAT,
                    recBufferSize
                )

                if (tempRecord.state != AudioRecord.STATE_INITIALIZED) {
                    tempRecord.release()
                    continue
                }

                val tempTrack = AudioTrack.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(CHANNEL_CONFIG_OUT)
                            .build()
                    )
                    .setBufferSizeInBytes(trackBufferSize)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build()

                if (tempTrack.state != AudioTrack.STATE_INITIALIZED) {
                    tempRecord.release()
                    tempTrack.release()
                    continue
                }

                activeSampleRate = sampleRate
                audioRecord = tempRecord
                audioTrack = tempTrack

                // Re-initialize DSP modules with active sample rate
                pitchShifter = PitchShifter(activeSampleRate)
                formantShifter = FormantShifter(activeSampleRate)
                parametricEQ = ParametricEQ(activeSampleRate)
                reverb = Reverb(activeSampleRate)
                echo = Echo(activeSampleRate)
                vocalRoughness = VocalRoughness(activeSampleRate)
                pitchDetector = PitchDetector(activeSampleRate)
                updateParams(currentParams)

                recordInitialized = true
                Log.d(TAG, "Audio hardware initialized successfully at $sampleRate Hz")
                break
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Failed initializing audio at $sampleRate Hz: ${e.message}")
            }
        }

        if (!recordInitialized || audioRecord == null || audioTrack == null) {
            stopEngine()
            return Result.failure(lastError ?: IllegalStateException("Microphone or Audio output is occupied or unsupported on this device."))
        }

        return try {
            audioRecord?.startRecording()
            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                stopEngine()
                return Result.failure(IllegalStateException("Microphone is occupied by another application."))
            }

            audioTrack?.play()
            _isRunning.value = true

            engineJob = scope.launch {
                val pcmBuffer = ShortArray(BUFFER_SIZE)

                while (isActive && _isRunning.value) {
                    try {
                        val readSize = audioRecord?.read(pcmBuffer, 0, BUFFER_SIZE) ?: 0
                        if (readSize > 0) {
                            // 1. Auto Mode calculation if enabled
                            if (currentParams.isAutoModeEnabled) {
                                try {
                                    val detectedPitch = pitchDetector.detectPitch(pcmBuffer, readSize)
                                    if (detectedPitch > 0f) {
                                        val autoShifts = formantDetector.calculateAutoShift(detectedPitch, targetGenderIsFemale = true)
                                        pitchShifter.setPitchShiftSemitones(autoShifts.first)
                                        formantShifter.setFormantShiftFactor(autoShifts.second)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Pitch/Formant detector exception: ${e.message}")
                                }
                            }

                            // 2. DSP Pipeline with frame guards
                            var processed = pcmBuffer.copyOf(readSize)
                            try { processed = pitchShifter.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "PitchShifter error", e) }
                            try { processed = formantShifter.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "FormantShifter error", e) }
                            try { processed = parametricEQ.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "ParametricEQ error", e) }
                            try { processed = vocalRoughness.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "VocalRoughness error", e) }
                            try { processed = compressor.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "Compressor error", e) }
                            try { processed = deEsser.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "DeEsser error", e) }
                            try { processed = reverb.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "Reverb error", e) }
                            try { processed = echo.process(processed, readSize) } catch (e: Exception) { Log.e(TAG, "Echo error", e) }

                            // 3. Playback
                            audioTrack?.write(processed, 0, readSize)

                            // 4. Recording to File
                            if (isRecordingToFile && recordingFileOutputStream != null) {
                                try {
                                    val byteBuffer = ByteBuffer.allocate(readSize * 2).order(ByteOrder.LITTLE_ENDIAN)
                                    for (i in 0 until readSize) {
                                        byteBuffer.putShort(processed[i])
                                    }
                                    recordingFileOutputStream?.write(byteBuffer.array())
                                    totalPcmBytesWritten += readSize * 2
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error writing audio recording chunk", e)
                                }
                            }

                            // 5. Update Waveform and Audio Level
                            updateMetrics(processed, readSize)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Audio process frame error", e)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            stopEngine()
            Result.failure(e)
        }
    }

    private fun updateMetrics(buffer: ShortArray, size: Int) {
        var maxAmp = 0f
        val waveformSample = FloatArray(32)
        val step = maxOf(1, size / 32)

        for (i in 0 until 32) {
            val idx = i * step
            if (idx < size) {
                val norm = buffer[idx].toFloat() / 32768f
                waveformSample[i] = norm
                if (abs(norm) > maxAmp) maxAmp = abs(norm)
            }
        }
        _audioLevel.value = maxAmp
        _audioWaveform.value = waveformSample
    }

    fun stopEngine() {
        _isRunning.value = false
        engineJob?.cancel()
        engineJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}

        audioRecord = null
        audioTrack = null
        pitchDetector.reset()
    }

    fun startRecording(): Result<Unit> {
        return try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "VoxMorph")
            if (!dir.exists()) dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "VOXMORPH_$timestamp.wav")
            currentRecordingFile = file
            totalPcmBytesWritten = 0L

            val fos = FileOutputStream(file)
            writeWavHeader(fos, 0, 0) // Placeholder WAV header
            recordingFileOutputStream = fos
            isRecordingToFile = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<String> {
        isRecordingToFile = false
        val file = currentRecordingFile
        return try {
            recordingFileOutputStream?.flush()
            recordingFileOutputStream?.close()
            recordingFileOutputStream = null

            if (file != null && file.exists()) {
                // Update WAV header with exact byte sizes
                val raf = RandomAccessFile(file, "rw")
                updateWavHeader(raf, totalPcmBytesWritten)
                raf.close()
                Result.success(file.absolutePath)
            } else {
                Result.failure(IllegalStateException("Recorded file not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeWavHeader(out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long) {
        val channels = 1
        val byteRate = 16 * activeSampleRate * channels / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte(); header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte(); header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // 16-bit PCM format length
        header[20] = 1; header[21] = 0 // PCM format
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (activeSampleRate and 0xff).toByte(); header[25] = ((activeSampleRate shr 8) and 0xff).toByte()
        header[26] = ((activeSampleRate shr 16) and 0xff).toByte(); header[27] = ((activeSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte(); header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (16 * channels / 8).toByte(); header[33] = 0 // Block align
        header[34] = 16; header[35] = 0 // Bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte(); header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte(); header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }

    private fun updateWavHeader(raf: RandomAccessFile, pcmDataLen: Long) {
        val totalDataLen = pcmDataLen + 36

        raf.seek(4)
        raf.write(intToByteArray(totalDataLen.toInt()))
        raf.seek(40)
        raf.write(intToByteArray(pcmDataLen.toInt()))
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )
    }
}
