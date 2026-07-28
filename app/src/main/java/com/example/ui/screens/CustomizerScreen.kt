package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.DockCorner
import com.example.data.TimerConfig
import com.example.ui.components.ColorPickerSection
import com.example.ui.components.CornerPickerSection
import com.example.ui.components.PermissionsCard
import com.example.ui.components.PhoneMockupPreview
import com.example.ui.components.TimeLimitAndResetSection

@Composable
fun CustomizerScreen(
    config: TimerConfig,
    sessionSeconds: Long,
    totalSeconds: Long,
    isLocked: Boolean,
    hasOverlayPermission: Boolean,
    hasUsagePermission: Boolean,
    isOverlayServiceRunning: Boolean,
    onConfigChange: (TimerConfig) -> Unit,
    onSimulateLockToggle: () -> Unit,
    onResetSession: () -> Unit,
    onResetDailyCounter: () -> Unit,
    onCornerSelect: (DockCorner) -> Unit,
    onToggleService: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Interactive Phone Frame
        item {
            PhoneMockupPreview(
                config = config,
                sessionSeconds = sessionSeconds,
                totalSeconds = totalSeconds,
                isLocked = isLocked,
                onSimulateLockToggle = onSimulateLockToggle,
                onResetSession = onResetSession,
                onCornerSelect = onCornerSelect
            )
        }

        // 2. System Floating Overlay Permissions
        item {
            PermissionsCard(
                hasOverlayPermission = hasOverlayPermission,
                hasUsagePermission = hasUsagePermission,
                isOverlayServiceRunning = isOverlayServiceRunning,
                onToggleService = onToggleService
            )
        }

        // 3. Time Limit & Daily Reset Controls
        item {
            TimeLimitAndResetSection(
                config = config,
                onConfigChange = onConfigChange,
                onResetDailyCounter = onResetDailyCounter
            )
        }

        // 4. Color, Opacity, and Text Size Controls
        item {
            ColorPickerSection(
                config = config,
                onConfigChange = onConfigChange
            )
        }

        // 5. Corner Hugging Position & Orientation Controls
        item {
            CornerPickerSection(
                config = config,
                onConfigChange = onConfigChange
            )
        }
    }
}
