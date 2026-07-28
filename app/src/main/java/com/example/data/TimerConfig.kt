package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class DockCorner {
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    TOP_RIGHT,
    TOP_LEFT
}

enum class TextOrientation {
    VERTICAL_BOTTOM_TO_TOP,
    VERTICAL_TOP_TO_BOTTOM,
    HORIZONTAL
}

enum class FontStyleOption {
    MONOSPACE,
    SANS_SERIF,
    SERIF
}

data class TimerConfig(
    val textColorArgb: Int = 0xFF00E5FF.toInt(), // Cyber Cyan default
    val opacity: Float = 0.90f, // 10% to 100%
    val textSizeSp: Int = 18, // 10sp to 40sp
    val dockCorner: DockCorner = DockCorner.BOTTOM_RIGHT,
    val orientation: TextOrientation = TextOrientation.VERTICAL_BOTTOM_TO_TOP,
    val marginDp: Int = 12, // Offset from screen corner
    val showTotalScreenTime: Boolean = true,
    val showSessionTimer: Boolean = true,
    val showBackgroundCapsule: Boolean = true,
    val backgroundColorArgb: Int = 0x880D1117.toInt(), // Semi-transparent dark
    val fontStyle: FontStyleOption = FontStyleOption.MONOSPACE,
    val enableTimeLimit: Boolean = true,
    val timeLimitMinutes: Int = 60, // Screen time limit in minutes
    val autoDailyReset: Boolean = true
) {
    val composeTextColor: Color
        get() = Color(textColorArgb).copy(alpha = opacity)

    val composeBackgroundColor: Color
        get() = if (showBackgroundCapsule) Color(backgroundColorArgb).copy(alpha = opacity * 0.7f) else Color.Transparent
}
