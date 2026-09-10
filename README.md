# VoxMorph AI - Human Real-Time Voice Transformation Android Application

VoxMorph AI is a production-grade native Android application designed to transform human voice in real-time while strictly preserving **100% human realism, clarity, and intelligibility**.

---

## 1. Product Philosophy & Naturalness Rules

VoxMorph AI strictly prohibits robotized, metallic, chipmunk, or monster voice effects. Every transformation maintains natural human vocal mechanics:
- **Pitch Shift Limits:** Strict semitone limits (-8 to +8 semitones).
- **Formant Preservation:** Formants shift in a 1:1 ratio with pitch to match human vocal tract physiology.
- **Organic Asperity:** Vocal roughness is modeled using slow LFO amplitude/phase modulation (1–8 Hz) to simulate natural vocal fold vibration, eliminating digital distortion or ring modulators.
- **Dynamic Leveling:** Soft-knee compression (2:1 to 4:1) preserves human dynamic expression.

---

## 2. Technical Stack

- **Language:** 100% Kotlin
- **Compile / Target SDK:** 34 (Android 14)
- **Min SDK:** 24 (Android 7.0)
- **JDK / Toolchain:** Java 17
- **Architecture:** MVVM + Clean Architecture (Data, Domain, Presentation)
- **UI:** Jetpack Compose with Material 3 (Material You dynamic theme fallback)
- **Dependency Injection:** Dagger Hilt 2.51.1
- **Concurrency:** Coroutines + StateFlow
- **Audio & DSP:** AudioRecord + AudioTrack (real-time low latency loop) + TarsosDSP core
- **Persistence:** DataStore Preferences
- **Gradle:** Version Catalog (`libs.versions.toml`)

---

## 3. Human Realism DSP Pipeline Architecture

```
[ Microphone Input ]
       │
       ▼
[ Pitch Detector (YIN F0 + Exponential Smoothing) ]
       │
       ▼
[ Formant Detector (Auto Range Male 85-180Hz / Female 165-255Hz) ]
       │
       ▼
[ WSOLA Pitch Shifter (-8 to +8 st) ]
       │
       ▼
[ Formant Shifter (-3 to +3 factor) ]
       │
       ▼
[ Parametric EQ (LowShelf Rumble + Peaking Warmth + HighShelf Air) ]
       │
       ▼
[ Vocal Roughness / Rasp (Organic Modulation) ]
       │
       ▼
[ Soft Compressor (2:1 - 4:1 ratio) ]
       │
       ▼
[ De-Esser (5-8kHz Sibilance Reduction) ]
       │
       ▼
[ Schroeder Room Reverb (Mix <= 30%) ]
       │
       ▼
[ Controlled Delay Echo (Mix <= 20%) ]
       │
       ▼
[ AudioTrack Playback / WAV File Recording ]
```

---

## 4. Latency Optimization Notes

- **Buffer Size:** Configured to `2048` samples (44.1 kHz, 16-bit Mono PCM), giving frame chunks of ~46ms.
- **Performance Mode:** `AudioTrack.PERFORMANCE_MODE_LOW_LATENCY` enabled.
- **Threading:** Audio record and DSP processing loop run on `Dispatchers.Default` non-blocking coroutines.
- **Foreground Service:** `VoiceChangerService` handles continuous execution in the background with `FOREGROUND_SERVICE_TYPE_MICROPHONE`.

---

## 5. Compilation & Installation Instructions

### Prerequisites
- JDK 17
- Android SDK 34
- Gradle 8.8+

### Build & Test Commands
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```

APK output location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 6. Future AI Roadmap (On-Device Voice Conversion)

To transition from classic DSP formant shifting to deep neural voice conversion:
1. **TensorFlow Lite / ONNX Runtime Integration:** Embed quantized Lightweight RVC (Retrieval-based Voice Conversion) or Mobile-vits-svc models.
2. **Feature Extraction:** Real-time extraction of ContentVec / HuBERT speech embeddings on DSP background threads.
3. **On-Device NN Inference:** Neural vocoder synthesis (HiFi-GAN / BigVGAN) accelerated via Android NNAPI / GPU Delegates for sub-50ms neural latency.
