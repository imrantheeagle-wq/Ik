package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FontStyleOption
import com.example.data.TextOrientation
import com.example.data.TimerConfig
import com.example.util.formatDuration

@Composable
fun OverlayTimerView(
    config: TimerConfig,
    sessionSeconds: Long,
    totalSeconds: Long,
    modifier: Modifier = Modifier
) {
    val totalFormatted = formatDuration(totalSeconds)
    val sessionFormatted = formatDuration(sessionSeconds)

    val limitSeconds = config.timeLimitMinutes * 60L
    val isExceeded = config.enableTimeLimit && limitSeconds > 0 && totalSeconds >= limitSeconds
    val exceededSeconds = if (isExceeded) totalSeconds - limitSeconds else 0L

    // Scale increases by every second exceeded (0.05x per second, up to 3.0x max)
    val scaleFactor = if (isExceeded) {
        1.0f + minOf(exceededSeconds * 0.05f, 2.0f)
    } else 1.0f

    // Pulsing Red Glow
    val infiniteTransition = rememberInfiniteTransition(label = "RedGlowAnimation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val activeTextColor = if (isExceeded) {
        Color(0xFFFF1744).copy(alpha = pulseAlpha)
    } else {
        config.composeTextColor
    }

    val activeBgColor = if (isExceeded) {
        Color(0xFF3B0008).copy(alpha = 0.92f)
    } else {
        config.composeBackgroundColor
    }

    val activeBorderColor = if (isExceeded) {
        Color(0xFFFF1744).copy(alpha = pulseAlpha)
    } else {
        config.composeTextColor.copy(alpha = config.opacity * 0.4f)
    }

    val fontFam = when (config.fontStyle) {
        FontStyleOption.MONOSPACE -> FontFamily.Monospace
        FontStyleOption.SANS_SERIF -> FontFamily.SansSerif
        FontStyleOption.SERIF -> FontFamily.Serif
    }

    val rotationAngle = when (config.orientation) {
        TextOrientation.VERTICAL_BOTTOM_TO_TOP -> -90f
        TextOrientation.VERTICAL_TOP_TO_BOTTOM -> 90f
        TextOrientation.HORIZONTAL -> 0f
    }

    val capsuleModifier = if (config.showBackgroundCapsule || isExceeded) {
        Modifier
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .background(
                color = activeBgColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isExceeded) 2.dp else 1.dp,
                color = activeBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    } else {
        Modifier
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .padding(4.dp)
    }

    Box(
        modifier = modifier
            .then(capsuleModifier)
            .rotate(rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isExceeded) {
                Text(
                    text = "⚠️ EXCEEDED! +${formatDuration(exceededSeconds)}",
                    color = Color(0xFFFF5252),
                    fontSize = (config.textSizeSp * 0.65f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFam,
                    letterSpacing = 0.5.sp
                )
            }

            if (config.showTotalScreenTime) {
                Text(
                    text = "TODAY $totalFormatted",
                    color = activeTextColor,
                    fontSize = (config.textSizeSp * 0.85f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFam,
                    letterSpacing = 0.5.sp
                )
            }

            if (config.showSessionTimer) {
                Text(
                    text = "ON $sessionFormatted",
                    color = if (isExceeded) Color(0xFFFF8A80) else config.composeTextColor.copy(alpha = config.opacity * 0.9f),
                    fontSize = (config.textSizeSp * 0.72f).sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = fontFam
                )
            }
        }
    }
}
