package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DockCorner
import com.example.data.TimerConfig
import com.example.data.TimerPreferences
import com.example.data.UnlockSessionEntity
import com.example.service.ScreenTimerOverlayService
import com.example.service.UnlockBroadcastReceiver
import com.example.service.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = TimerPreferences(application)
    val configState: StateFlow<TimerConfig> = prefs.configFlow

    private val db = AppDatabase.getInstance(application)
    private val timerDao = db.timerDao()

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val sessionsTodayState: StateFlow<List<UnlockSessionEntity>> = timerDao.getSessionsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSecondsTodayFromDb: StateFlow<Long?> = timerDao.getTotalSecondsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val unlockCountTodayFromDb: StateFlow<Int> = timerDao.getUnlockCountForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _sessionSeconds = MutableStateFlow(0L)
    val sessionSeconds: StateFlow<Long> = _sessionSeconds.asStateFlow()

    private val _totalScreenTimeSeconds = MutableStateFlow(0L)
    val totalScreenTimeSeconds: StateFlow<Long> = _totalScreenTimeSeconds.asStateFlow()

    private val _isSimulatedLocked = MutableStateFlow(false)
    val isSimulatedLocked: StateFlow<Boolean> = _isSimulatedLocked.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(false)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private var lastCheckedDate = todayDateString
    private var manualResetBaseSeconds = 0L

    init {
        checkPermissions()
        startTicker()
    }

    fun checkPermissions() {
        val app = getApplication<Application>()
        _hasOverlayPermission.value = Settings.canDrawOverlays(app)
        _hasUsagePermission.value = UsageStatsHelper.hasUsagePermission(app)
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                val app = getApplication<Application>()
                checkPermissions()

                // Check Midnight Auto Reset
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (configState.value.autoDailyReset && currentDate != lastCheckedDate) {
                    lastCheckedDate = currentDate
                    resetDailyCounters()
                }

                if (!_isSimulatedLocked.value) {
                    val currentSession = UnlockBroadcastReceiver.getCurrentSessionDurationSeconds()
                    _sessionSeconds.value = currentSession

                    val realUsage = UsageStatsHelper.getTodayScreenTimeSeconds(app)
                    val dbSum = totalSecondsTodayFromDb.value ?: 0L

                    val rawTotal = when {
                        realUsage > 0 -> realUsage
                        dbSum > 0 -> dbSum + currentSession
                        else -> currentSession
                    }

                    _totalScreenTimeSeconds.value = maxOf(0L, rawTotal - manualResetBaseSeconds)
                }

                delay(1000)
            }
        }
    }

    fun resetDailyCounters() {
        UnlockBroadcastReceiver.resetSessionStartTime()
        _sessionSeconds.value = 0L
        val app = getApplication<Application>()
        val realUsage = UsageStatsHelper.getTodayScreenTimeSeconds(app)
        val dbSum = totalSecondsTodayFromDb.value ?: 0L
        val rawTotal = if (realUsage > 0) realUsage else dbSum
        manualResetBaseSeconds = rawTotal
        _totalScreenTimeSeconds.value = 0L
    }

    fun updateConfig(newConfig: TimerConfig) {
        prefs.saveConfig(newConfig)
    }

    fun toggleSimulatedLock() {
        _isSimulatedLocked.value = !_isSimulatedLocked.value
        if (!_isSimulatedLocked.value) {
            UnlockBroadcastReceiver.resetSessionStartTime()
        }
    }

    fun resetSessionTimer() {
        UnlockBroadcastReceiver.resetSessionStartTime()
        _sessionSeconds.value = 0L
    }

    fun setOverlayServiceState(run: Boolean) {
        val app = getApplication<Application>()
        _isOverlayServiceRunning.value = run
        if (run) {
            ScreenTimerOverlayService.startService(app)
        } else {
            ScreenTimerOverlayService.stopService(app)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            timerDao.clearAll()
        }
    }
}
