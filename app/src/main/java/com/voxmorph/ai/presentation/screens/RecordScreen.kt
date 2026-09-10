package com.voxmorph.ai.presentation.screens

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.voxmorph.ai.R
import com.voxmorph.ai.presentation.theme.ActiveActiveGreen
import com.voxmorph.ai.presentation.theme.DeepBlue
import com.voxmorph.ai.presentation.theme.SoftGold
import com.voxmorph.ai.presentation.viewmodel.VoiceUiState
import java.io.File

@Composable
fun RecordScreen(
    uiState: VoiceUiState,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var playingFilePath by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.record_title),
            style = MaterialTheme.typography.headlineLarge,
            color = SoftGold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onToggleRecording,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isRecording) ActiveActiveGreen else DeepBlue
            )
        ) {
            Icon(
                imageVector = Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = SoftGold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (uiState.isRecording) stringResource(R.string.record_stop) else stringResource(R.string.record_start),
                style = MaterialTheme.typography.titleLarge,
                color = SoftGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.saved_recordings),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.recordedFiles.isEmpty()) {
            Text(
                text = stringResource(R.string.no_recordings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recordedFiles) { filePath ->
                    val file = File(filePath)
                    val isPlaying = playingFilePath == filePath

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepBlue)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SoftGold
                                )
                                Text(
                                    text = "${file.length() / 1024} KB",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Play / Pause Button
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        playingFilePath = null
                                    } else {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(filePath)
                                            prepare()
                                            start()
                                            setOnCompletionListener {
                                                playingFilePath = null
                                            }
                                        }
                                        playingFilePath = filePath
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = SoftGold
                                )
                            }

                            // Share Intent Button
                            IconButton(
                                onClick = {
                                    val uri: Uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = SoftGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
