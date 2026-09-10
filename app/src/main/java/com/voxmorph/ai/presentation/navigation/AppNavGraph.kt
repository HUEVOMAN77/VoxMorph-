package com.voxmorph.ai.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voxmorph.ai.R
import com.voxmorph.ai.presentation.screens.HomeScreen
import com.voxmorph.ai.presentation.screens.ManualScreen
import com.voxmorph.ai.presentation.screens.RecordScreen
import com.voxmorph.ai.presentation.theme.DeepBlue
import com.voxmorph.ai.presentation.theme.SoftGold
import com.voxmorph.ai.presentation.viewmodel.VoiceViewModel

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.GraphicEq)
    object Manual : Screen("manual", R.string.nav_manual, Icons.Default.Tune)
    object Record : Screen("record", R.string.nav_record, Icons.Default.Mic)
}

@Composable
fun AppNavGraph(
    viewModel: VoiceViewModel,
    uiState: com.voxmorph.ai.presentation.viewmodel.VoiceUiState,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Home,
        Screen.Manual,
        Screen.Record
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlue
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(id = screen.titleRes)
                            )
                        },
                        label = { Text(stringResource(id = screen.titleRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlue,
                            selectedTextColor = SoftGold,
                            indicatorColor = SoftGold,
                            unselectedIconColor = SoftGold.copy(alpha = 0.6f),
                            unselectedTextColor = SoftGold.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onToggleEngine = { viewModel.toggleEngine() },
                    onSelectPreset = { viewModel.selectPreset(it.type) },
                    onToggleAutoMode = { viewModel.toggleAutoMode(it) }
                )
            }
            composable(Screen.Manual.route) {
                ManualScreen(
                    uiState = uiState,
                    onParamsChange = { viewModel.updateParams(it) },
                    onResetParams = { viewModel.resetToNatural() }
                )
            }
            composable(Screen.Record.route) {
                RecordScreen(
                    uiState = uiState,
                    onToggleRecording = { viewModel.toggleRecording() }
                )
            }
        }
    }
}
