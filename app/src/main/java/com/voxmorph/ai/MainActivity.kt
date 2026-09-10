package com.voxmorph.ai

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.voxmorph.ai.presentation.navigation.AppNavGraph
import com.voxmorph.ai.presentation.theme.VoxMorphTheme
import com.voxmorph.ai.presentation.viewmodel.VoiceViewModel
import com.voxmorph.ai.service.VoiceChangerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VoiceViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VoiceChangerService.createNotificationChannel(this)

        setContent {
            VoxMorphTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Permissions handling
                val permissions = mutableListOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                ).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        add(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                    }
                }

                val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

                LaunchedEffect(Unit) {
                    if (!permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }

                LaunchedEffect(uiState.isEngineRunning) {
                    if (uiState.isEngineRunning) {
                        if (permissionsState.allPermissionsGranted) {
                            val intent = Intent(this@MainActivity, VoiceChangerService::class.java).apply {
                                action = VoiceChangerService.ACTION_START
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                        } else {
                            viewModel.toggleEngine()
                            permissionsState.launchMultiplePermissionRequest()
                            snackbarHostState.showSnackbar(getString(R.string.permission_mic_required))
                        }
                    } else {
                        val intent = Intent(this@MainActivity, VoiceChangerService::class.java).apply {
                            action = VoiceChangerService.ACTION_STOP
                        }
                        startService(intent)
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let { error ->
                        snackbarHostState.showSnackbar(error)
                        viewModel.clearError()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        AppNavGraph(
                            viewModel = viewModel,
                            uiState = uiState,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
