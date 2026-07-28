package com.example.service

import com.example.ui.components.OverlayTimerView

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.util.formatDuration
import com.example.R
import com.example.data.DockCorner
import com.example.data.FontStyleOption
import com.example.data.TextOrientation
import com.example.data.TimerConfig
import com.example.data.TimerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScreenTimerOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var timerPreferences: TimerPreferences? = null

    private var timerJob: Job? = null
    private val sessionSecondsFlow = MutableStateFlow(0L)
    private val totalSecondsFlow = MutableStateFlow(0L)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        timerPreferences = TimerPreferences(this)
        startForegroundNotification()

        if (Settings.canDrawOverlays(this)) {
            setupOverlayWindow()
        }

        startTimerTicker()
    }

    private fun startForegroundNotification() {
        val channelId = "screen_timer_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Screen Timer Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Timer Active")
            .setContentText("Displaying live unlock session & total screen usage overlay")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val config = timerPreferences?.loadConfig() ?: TimerConfig()
        updateGravity(params, config.dockCorner, config.marginDp)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ScreenTimerOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ScreenTimerOverlayService)

            setContent {
                val currentConfig by timerPreferences?.configFlow?.collectAsState() ?: mutableStateOf(TimerConfig())
                val sessionSec by sessionSecondsFlow.collectAsState()
                val totalSec by totalSecondsFlow.collectAsState()

                OverlayTimerContent(
                    config = currentConfig,
                    sessionSeconds = sessionSec,
                    totalSeconds = totalSec
                )
            }
        }

        try {
            windowManager?.addView(composeView, params)
            overlayComposeView = composeView
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateGravity(params: WindowManager.LayoutParams, corner: DockCorner, marginDp: Int) {
        val px = (marginDp * resources.displayMetrics.density).toInt()
        when (corner) {
            DockCorner.BOTTOM_RIGHT -> {
                params.gravity = Gravity.BOTTOM or Gravity.END
                params.x = px
                params.y = px
            }
            DockCorner.BOTTOM_LEFT -> {
                params.gravity = Gravity.BOTTOM or Gravity.START
                params.x = px
                params.y = px
            }
            DockCorner.TOP_RIGHT -> {
                params.gravity = Gravity.TOP or Gravity.END
                params.x = px
                params.y = px
            }
            DockCorner.TOP_LEFT -> {
                params.gravity = Gravity.TOP or Gravity.START
                params.x = px
                params.y = px
            }
        }
    }

    private fun startTimerTicker() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentSessionSec = UnlockBroadcastReceiver.getCurrentSessionDurationSeconds()
                sessionSecondsFlow.value = currentSessionSec

                val usageSec = UsageStatsHelper.getTodayScreenTimeSeconds(this@ScreenTimerOverlayService)
                totalSecondsFlow.value = if (usageSec > 0) usageSec else currentSessionSec

                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
        timerJob?.cancel()
        overlayComposeView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, ScreenTimerOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ScreenTimerOverlayService::class.java)
            context.stopService(intent)
        }
    }
}

@Composable
fun OverlayTimerContent(
    config: TimerConfig,
    sessionSeconds: Long,
    totalSeconds: Long
) {
    OverlayTimerView(
        config = config,
        sessionSeconds = sessionSeconds,
        totalSeconds = totalSeconds
    )
}
