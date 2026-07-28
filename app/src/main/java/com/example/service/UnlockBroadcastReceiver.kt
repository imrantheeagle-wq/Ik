package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.UnlockSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnlockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val now = System.currentTimeMillis()

        when (action) {
            Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> {
                // Device Unlocked! Reset current session start time
                sessionStartTimeMillis = now
                isDeviceUnlocked.value = true
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Device Locked!
                isDeviceUnlocked.value = false
                val start = sessionStartTimeMillis
                if (start > 0) {
                    val durationSeconds = (now - start) / 1000L
                    if (durationSeconds > 0) {
                        saveSessionToDb(context, start, now, durationSeconds)
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                sessionStartTimeMillis = now
                isDeviceUnlocked.value = true
            }
        }
    }

    private fun saveSessionToDb(context: Context, unlockTime: Long, lockTime: Long, durationSec: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date(unlockTime))
        val db = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            db.timerDao().insertSession(
                UnlockSessionEntity(
                    unlockTimestamp = unlockTime,
                    lockTimestamp = lockTime,
                    durationSeconds = durationSec,
                    dateString = dateStr
                )
            )
        }
    }

    companion object {
        var sessionStartTimeMillis: Long = System.currentTimeMillis()
            private set

        private val isDeviceUnlocked = MutableStateFlow(true)
        val unlockedStateFlow: StateFlow<Boolean> = isDeviceUnlocked.asStateFlow()

        fun resetSessionStartTime() {
            sessionStartTimeMillis = System.currentTimeMillis()
            isDeviceUnlocked.value = true
        }

        fun getCurrentSessionDurationSeconds(): Long {
            val start = sessionStartTimeMillis
            if (start <= 0) return 0L
            val elapsed = (System.currentTimeMillis() - start) / 1000L
            return if (elapsed < 0) 0L else elapsed
        }
    }
}
