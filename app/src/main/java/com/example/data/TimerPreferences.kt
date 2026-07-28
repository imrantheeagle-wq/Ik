package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("screen_timer_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<TimerConfig> = _configFlow.asStateFlow()

    fun loadConfig(): TimerConfig {
        return TimerConfig(
            textColorArgb = prefs.getInt(KEY_COLOR, 0xFF00E5FF.toInt()),
            opacity = prefs.getFloat(KEY_OPACITY, 0.90f),
            textSizeSp = prefs.getInt(KEY_SIZE, 18),
            dockCorner = try {
                DockCorner.valueOf(prefs.getString(KEY_CORNER, DockCorner.BOTTOM_RIGHT.name) ?: DockCorner.BOTTOM_RIGHT.name)
            } catch (e: Exception) { DockCorner.BOTTOM_RIGHT },
            orientation = try {
                TextOrientation.valueOf(prefs.getString(KEY_ORIENTATION, TextOrientation.VERTICAL_BOTTOM_TO_TOP.name) ?: TextOrientation.VERTICAL_BOTTOM_TO_TOP.name)
            } catch (e: Exception) { TextOrientation.VERTICAL_BOTTOM_TO_TOP },
            marginDp = prefs.getInt(KEY_MARGIN, 12),
            showTotalScreenTime = prefs.getBoolean(KEY_SHOW_TOTAL, true),
            showSessionTimer = prefs.getBoolean(KEY_SHOW_SESSION, true),
            showBackgroundCapsule = prefs.getBoolean(KEY_SHOW_BG, true),
            backgroundColorArgb = prefs.getInt(KEY_BG_COLOR, 0x880D1117.toInt()),
            fontStyle = try {
                FontStyleOption.valueOf(prefs.getString(KEY_FONT_STYLE, FontStyleOption.MONOSPACE.name) ?: FontStyleOption.MONOSPACE.name)
            } catch (e: Exception) { FontStyleOption.MONOSPACE },
            enableTimeLimit = prefs.getBoolean(KEY_ENABLE_LIMIT, true),
            timeLimitMinutes = prefs.getInt(KEY_LIMIT_MINS, 60),
            autoDailyReset = prefs.getBoolean(KEY_AUTO_RESET, true)
        )
    }

    fun saveConfig(config: TimerConfig) {
        prefs.edit().apply {
            putInt(KEY_COLOR, config.textColorArgb)
            putFloat(KEY_OPACITY, config.opacity)
            putInt(KEY_SIZE, config.textSizeSp)
            putString(KEY_CORNER, config.dockCorner.name)
            putString(KEY_ORIENTATION, config.orientation.name)
            putInt(KEY_MARGIN, config.marginDp)
            putBoolean(KEY_SHOW_TOTAL, config.showTotalScreenTime)
            putBoolean(KEY_SHOW_SESSION, config.showSessionTimer)
            putBoolean(KEY_SHOW_BG, config.showBackgroundCapsule)
            putInt(KEY_BG_COLOR, config.backgroundColorArgb)
            putString(KEY_FONT_STYLE, config.fontStyle.name)
            putBoolean(KEY_ENABLE_LIMIT, config.enableTimeLimit)
            putInt(KEY_LIMIT_MINS, config.timeLimitMinutes)
            putBoolean(KEY_AUTO_RESET, config.autoDailyReset)
            apply()
        }
        _configFlow.value = config
    }

    companion object {
        private const val KEY_COLOR = "text_color_argb"
        private const val KEY_OPACITY = "opacity"
        private const val KEY_SIZE = "text_size_sp"
        private const val KEY_CORNER = "dock_corner"
        private const val KEY_ORIENTATION = "text_orientation"
        private const val KEY_MARGIN = "margin_dp"
        private const val KEY_SHOW_TOTAL = "show_total"
        private const val KEY_SHOW_SESSION = "show_session"
        private const val KEY_SHOW_BG = "show_bg"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_FONT_STYLE = "font_style"
        private const val KEY_ENABLE_LIMIT = "enable_time_limit"
        private const val KEY_LIMIT_MINS = "time_limit_mins"
        private const val KEY_AUTO_RESET = "auto_daily_reset"
    }
}
