package com.voxmorph.ai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.voxmorph.ai.R
import com.voxmorph.ai.domain.model.VoiceParams
import com.voxmorph.ai.presentation.components.ParamSlider
import com.voxmorph.ai.presentation.theme.DeepBlue
import com.voxmorph.ai.presentation.theme.SoftGold
import com.voxmorph.ai.presentation.viewmodel.VoiceUiState
import java.util.Locale

@Composable
fun ManualScreen(
    uiState: VoiceUiState,
    onParamsChange: (VoiceParams) -> Unit,
    onResetParams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val params = uiState.params

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_manual),
            style = MaterialTheme.typography.headlineLarge,
            color = SoftGold
        )

        // Pitch Shift (-8 to +8)
        ParamSlider(
            title = stringResource(R.string.param_pitch),
            description = stringResource(R.string.param_pitch_desc),
            value = params.pitchShiftSemitones,
            valueRange = -8f..8f,
            valueLabel = String.format(Locale.US, "%.1f st", params.pitchShiftSemitones),
            onValueChange = { onParamsChange(params.copy(pitchShiftSemitones = it)) }
        )

        // Formant Shift (-3 to +3)
        ParamSlider(
            title = stringResource(R.string.param_formant),
            description = stringResource(R.string.param_formant_desc),
            value = params.formantShiftFactor,
            valueRange = -3f..3f,
            valueLabel = String.format(Locale.US, "%.1f", params.formantShiftFactor),
            onValueChange = { onParamsChange(params.copy(formantShiftFactor = it)) }
        )

        // Rumble / Low End (0% to 60%)
        ParamSlider(
            title = stringResource(R.string.param_rumble),
            description = stringResource(R.string.param_rumble_desc),
            value = params.rumblePercent,
            valueRange = 0f..0.6f,
            valueLabel = String.format(Locale.US, "%d%%", (params.rumblePercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(rumblePercent = it)) }
        )

        // Rasp / Vocal Roughness (0% to 70%)
        ParamSlider(
            title = stringResource(R.string.param_rasp),
            description = stringResource(R.string.param_rasp_desc),
            value = params.raspPercent,
            valueRange = 0f..0.7f,
            valueLabel = String.format(Locale.US, "%d%%", (params.raspPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(raspPercent = it)) }
        )

        // Warmth / Body (0% to 100%)
        ParamSlider(
            title = stringResource(R.string.param_warmth),
            description = stringResource(R.string.param_warmth_desc),
            value = params.warmthPercent,
            valueRange = 0f..1.0f,
            valueLabel = String.format(Locale.US, "%d%%", (params.warmthPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(warmthPercent = it)) }
        )

        // Breathiness (0% to 40%)
        ParamSlider(
            title = stringResource(R.string.param_breath),
            description = stringResource(R.string.param_breath_desc),
            value = params.breathinessPercent,
            valueRange = 0f..0.4f,
            valueLabel = String.format(Locale.US, "%d%%", (params.breathinessPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(breathinessPercent = it)) }
        )

        // Room Reverb (0% to 30%)
        ParamSlider(
            title = stringResource(R.string.param_reverb),
            description = stringResource(R.string.param_reverb_desc),
            value = params.reverbPercent,
            valueRange = 0f..0.3f,
            valueLabel = String.format(Locale.US, "%d%%", (params.reverbPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(reverbPercent = it)) }
        )

        // Echo (0% to 20%)
        ParamSlider(
            title = stringResource(R.string.param_echo),
            description = stringResource(R.string.param_echo_desc),
            value = params.echoPercent,
            valueRange = 0f..0.2f,
            valueLabel = String.format(Locale.US, "%d%%", (params.echoPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(echoPercent = it)) }
        )

        // Soft Compression (0% to 100%)
        ParamSlider(
            title = stringResource(R.string.param_compression),
            description = stringResource(R.string.param_compression_desc),
            value = params.compressionPercent,
            valueRange = 0f..1.0f,
            valueLabel = String.format(Locale.US, "%d%%", (params.compressionPercent * 100).toInt()),
            onValueChange = { onParamsChange(params.copy(compressionPercent = it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onResetParams,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftGold,
                contentColor = DeepBlue
            )
        ) {
            Text(
                text = stringResource(R.string.btn_reset),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
