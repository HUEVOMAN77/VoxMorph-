package com.voxmorph.ai.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.domain.model.VoicePresetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voxmorph_settings")

/**
 * DataStore preferences layer storing selected presets and custom parameter configurations.
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_PRESET = stringPreferencesKey("key_preset")
        private val KEY_PITCH = floatPreferencesKey("key_pitch")
        private val KEY_FORMANT = floatPreferencesKey("key_formant")
        private val KEY_RUMBLE = floatPreferencesKey("key_rumble")
        private val KEY_RASP = floatPreferencesKey("key_rasp")
        private val KEY_WARMTH = floatPreferencesKey("key_warmth")
        private val KEY_BREATH = floatPreferencesKey("key_breath")
        private val KEY_REVERB = floatPreferencesKey("key_reverb")
        private val KEY_ECHO = floatPreferencesKey("key_echo")
        private val KEY_COMPRESSION = floatPreferencesKey("key_compression")
        private val KEY_AUTO_MODE = booleanPreferencesKey("key_auto_mode")
    }

    val selectedPresetFlow: Flow<VoicePresetType> = context.dataStore.data.map { prefs ->
        val presetName = prefs[KEY_PRESET] ?: VoicePresetType.ORIGINAL.name
        try {
            VoicePresetType.valueOf(presetName)
        } catch (_: Exception) {
            VoicePresetType.ORIGINAL
        }
    }

    val voiceParamsFlow: Flow<VoiceParams> = context.dataStore.data.map { prefs ->
        VoiceParams(
            pitchShiftSemitones = prefs[KEY_PITCH] ?: 0f,
            formantShiftFactor = prefs[KEY_FORMANT] ?: 0f,
            rumblePercent = prefs[KEY_RUMBLE] ?: 0f,
            raspPercent = prefs[KEY_RASP] ?: 0f,
            warmthPercent = prefs[KEY_WARMTH] ?: 0f,
            breathinessPercent = prefs[KEY_BREATH] ?: 0f,
            reverbPercent = prefs[KEY_REVERB] ?: 0f,
            echoPercent = prefs[KEY_ECHO] ?: 0f,
            compressionPercent = prefs[KEY_COMPRESSION] ?: 0f,
            isAutoModeEnabled = prefs[KEY_AUTO_MODE] ?: false
        ).sanitize()
    }

    suspend fun savePreset(presetType: VoicePresetType) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRESET] = presetType.name
        }
    }

    suspend fun saveVoiceParams(params: VoiceParams) {
        val sanitized = params.sanitize()
        context.dataStore.edit { prefs ->
            prefs[KEY_PITCH] = sanitized.pitchShiftSemitones
            prefs[KEY_FORMANT] = sanitized.formantShiftFactor
            prefs[KEY_RUMBLE] = sanitized.rumblePercent
            prefs[KEY_RASP] = sanitized.raspPercent
            prefs[KEY_WARMTH] = sanitized.warmthPercent
            prefs[KEY_BREATH] = sanitized.breathinessPercent
            prefs[KEY_REVERB] = sanitized.reverbPercent
            prefs[KEY_ECHO] = sanitized.echoPercent
            prefs[KEY_COMPRESSION] = sanitized.compressionPercent
            prefs[KEY_AUTO_MODE] = sanitized.isAutoModeEnabled
        }
    }
}
