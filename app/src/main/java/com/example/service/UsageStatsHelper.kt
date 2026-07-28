package com.example.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    /**
     * Calculates the aggregate screen interactive/usage time in seconds for today (00:00:00 to now)
     */
    fun getTodayScreenTimeSeconds(context: Context): Long {
        if (!hasUsagePermission(context)) return 0L

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        var totalUsageMillis = 0L

        try {
            val events = usm.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()

            var lastInteractiveTime: Long? = null

            while (events.hasNextEvent()) {
                events.getNextEvent(event)

                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE, UsageEvents.Event.USER_INTERACTION -> {
                        if (lastInteractiveTime == null) {
                            lastInteractiveTime = event.timeStamp
                        }
                    }
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        if (lastInteractiveTime != null) {
                            totalUsageMillis += (event.timeStamp - lastInteractiveTime)
                            lastInteractiveTime = null
                        }
                    }
                }
            }

            // If screen is currently interactive/unlocked right now
            lastInteractiveTime?.let {
                totalUsageMillis += (endTime - it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return totalUsageMillis / 1000L
    }
}
