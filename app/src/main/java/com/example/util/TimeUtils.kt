package com.example.util

fun formatDuration(totalSec: Long): String {
    if (totalSec <= 0) return "00m 00s"
    val hours = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60

    return if (hours > 0) {
        String.format("%dh %02dm %02ds", hours, mins, secs)
    } else {
        String.format("%02dm %02ds", mins, secs)
    }
}
