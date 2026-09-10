package com.voxmorph.ai.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voxmorph.ai.R
import com.voxmorph.ai.domain.model.VoicePreset
import com.voxmorph.ai.presentation.components.PresetCard
import com.voxmorph.ai.presentation.components.WaveformVisualizer
import com.voxmorph.ai.presentation.theme.ActiveActiveGreen
import com.voxmorph.ai.presentation.theme.DeepBlue
import com.voxmorph.ai.presentation.theme.SoftGold
import com.voxmorph.ai.presentation.viewmodel.VoiceUiState

@Composable
fun HomeScreen(
    uiState: VoiceUiState,
    onToggleEngine: () -> Unit,
    onSelectPreset: (VoicePreset) -> Unit,
    onToggleAutoMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isEngineRunning) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = SoftGold
            )
            Text(
                text = if (uiState.isEngineRunning) stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.isEngineRunning) ActiveActiveGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Real-time Waveform & Central Mic Toggle Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            WaveformVisualizer(
                waveform = uiState.waveform,
                isEngineRunning = uiState.isEngineRunning
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .background(
                        color = if (uiState.isEngineRunning) SoftGold else DeepBlue,
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = SoftGold,
                        shape = CircleShape
                    )
                    .clickable { onToggleEngine() }
            ) {
                Icon(
                    imageVector = if (uiState.isEngineRunning) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle Engine",
                    tint = if (uiState.isEngineRunning) DeepBlue else SoftGold,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        // Auto Mode Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.auto_mode_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftGold
                    )
                    Text(
                        text = stringResource(R.string.auto_mode_desc),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = uiState.params.isAutoModeEnabled,
                    onCheckedChange = onToggleAutoMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepBlue,
                        checkedTrackColor = SoftGold
                    )
                )
            }
        }

        // Presets LazyRow Carousel
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.presets_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(VoicePreset.ALL_PRESETS) { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = uiState.selectedPreset == preset.type,
                        onClick = { onSelectPreset(preset) }
                    )
                }
            }
        }
    }
}
