package com.voxmorph.ai.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.voxmorph.ai.presentation.theme.SoftGold

/**
 * Real-time audio waveform visualizer rendered on Canvas.
 */
@Composable
fun WaveformVisualizer(
    waveform: FloatArray,
    isEngineRunning: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = SoftGold
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val count = waveform.size
        val barWidth = width / (count * 2f)

        for (i in 0 until count) {
            val amp = if (isEngineRunning) waveform[i].coerceIn(-1f, 1f) else 0.05f
            val barHeight = (amp * centerY * 0.8f).coerceAtLeast(4f)
            val startX = i * (barWidth * 2f) + barWidth

            drawLine(
                color = barColor.copy(alpha = if (isEngineRunning) 0.9f else 0.3f),
                start = Offset(startX, centerY - barHeight),
                end = Offset(startX, centerY + barHeight),
                strokeWidth = barWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}
