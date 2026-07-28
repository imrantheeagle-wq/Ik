package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CustomizerScreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimerApp(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val config by viewModel.configState.collectAsState()
    val sessionSeconds by viewModel.sessionSeconds.collectAsState()
    val totalSeconds by viewModel.totalScreenTimeSeconds.collectAsState()
    val isLocked by viewModel.isSimulatedLocked.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val isOverlayServiceRunning by viewModel.isOverlayServiceRunning.collectAsState()

    val sessionsToday by viewModel.sessionsTodayState.collectAsState()
    val unlockCountToday by viewModel.unlockCountTodayFromDb.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Screen Timer & Customizer" else "Screen Usage Analytics",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Palette else Icons.Outlined.Palette,
                            contentDescription = "Timer Customizer"
                        )
                    },
                    label = { Text("Timer & Layout") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Daily Usage") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "ScreenTransition") { tab ->
                when (tab) {
                    0 -> CustomizerScreen(
                        config = config,
                        sessionSeconds = sessionSeconds,
                        totalSeconds = totalSeconds,
                        isLocked = isLocked,
                        hasOverlayPermission = hasOverlayPermission,
                        hasUsagePermission = hasUsagePermission,
                        isOverlayServiceRunning = isOverlayServiceRunning,
                        onConfigChange = { viewModel.updateConfig(it) },
                        onSimulateLockToggle = { viewModel.toggleSimulatedLock() },
                        onResetSession = { viewModel.resetSessionTimer() },
                        onResetDailyCounter = { viewModel.resetDailyCounters() },
                        onCornerSelect = { corner -> viewModel.updateConfig(config.copy(dockCorner = corner)) },
                        onToggleService = { viewModel.setOverlayServiceState(it) }
                    )
                    1 -> AnalyticsScreen(
                        totalScreenTimeSeconds = totalSeconds,
                        unlockCountToday = if (unlockCountToday > 0) unlockCountToday else if (sessionsToday.isNotEmpty()) sessionsToday.size else 1,
                        sessions = sessionsToday,
                        onClearHistory = { viewModel.clearSessionHistory() }
                    )
                }
            }
        }
    }
}
