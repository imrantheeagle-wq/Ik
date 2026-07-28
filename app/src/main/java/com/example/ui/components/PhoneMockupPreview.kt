package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DockCorner
import com.example.data.FontStyleOption
import com.example.data.TextOrientation
import com.example.data.TimerConfig
import com.example.util.formatDuration

@Composable
fun PhoneMockupPreview(
    config: TimerConfig,
    sessionSeconds: Long,
    totalSeconds: Long,
    isLocked: Boolean,
    onSimulateLockToggle: () -> Unit,
    onResetSession: () -> Unit,
    onCornerSelect: (DockCorner) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE DISPLAY PREVIEW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Real-time vertical timer corner positioning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = onResetSession,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onSimulateLockToggle,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isLocked) "Locked" else "Unlocked", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smartphone Frame Mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F172A), // Dark slate wallpaper
                                Color(0xFF1E1B4B), // Deep violet accent
                                Color(0xFF020617)  // Obsidian bottom
                            )
                        )
                    )
            ) {
                // Notch
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .width(90.dp)
                        .height(18.dp)
                        .background(Color.Black, shape = RoundedCornerShape(10.dp))
                )

                // Simulated Lock Screen / Home Screen Overlay
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SCREEN LOCKED",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Session paused. Unlock phone to resume timer",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // Decorative Mock App Icons on screen wallpaper
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "10:42",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Tuesday, July 28",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // App icon grid simulation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                // Interactive Corner Tappable Hotspots
                CornerTappableOverlay(
                    selectedCorner = config.dockCorner,
                    onCornerSelect = onCornerSelect
                )

                // Actual Custom Vertical Timer Overlay Element
                if (!isLocked) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val marginPx = (config.marginDp * 0.8f).dp

                        val alignment = when (config.dockCorner) {
                            DockCorner.BOTTOM_RIGHT -> Alignment.BottomEnd
                            DockCorner.BOTTOM_LEFT -> Alignment.BottomStart
                            DockCorner.TOP_RIGHT -> Alignment.TopEnd
                            DockCorner.TOP_LEFT -> Alignment.TopStart
                        }

                        val rotationAngle = when (config.orientation) {
                            TextOrientation.VERTICAL_BOTTOM_TO_TOP -> -90f // Bottom to top
                            TextOrientation.VERTICAL_TOP_TO_BOTTOM -> 90f  // Top to bottom
                            TextOrientation.HORIZONTAL -> 0f
                        }

                        val fontFam = when (config.fontStyle) {
                            FontStyleOption.MONOSPACE -> FontFamily.Monospace
                            FontStyleOption.SANS_SERIF -> FontFamily.SansSerif
                            FontStyleOption.SERIF -> FontFamily.Serif
                        }

                        val formattedTotal = formatDuration(totalSeconds)
                        val formattedSession = formatDuration(sessionSeconds)

                        OverlayTimerView(
                            config = config,
                            sessionSeconds = sessionSeconds,
                            totalSeconds = totalSeconds,
                            modifier = Modifier
                                .align(alignment)
                                .padding(marginPx)
                        )
                    }
                }

                // Bottom Gesture Navigation Bar Line
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .width(100.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun CornerTappableOverlay(
    selectedCorner: DockCorner,
    onCornerSelect: (DockCorner) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val corners = listOf(
            DockCorner.TOP_LEFT to Alignment.TopStart,
            DockCorner.TOP_RIGHT to Alignment.TopEnd,
            DockCorner.BOTTOM_LEFT to Alignment.BottomStart,
            DockCorner.BOTTOM_RIGHT to Alignment.BottomEnd
        )

        corners.forEach { (corner, align) ->
            val isSelected = selectedCorner == corner
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 0.6f else 0.15f,
                animationSpec = tween(300),
                label = "cornerAlpha"
            )

            Box(
                modifier = Modifier
                    .align(align)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                        else Color.White.copy(alpha = alpha)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onCornerSelect(corner) }
            )
        }
    }
}
