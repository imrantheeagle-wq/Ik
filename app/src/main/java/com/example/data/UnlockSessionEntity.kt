package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_sessions")
data class UnlockSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val unlockTimestamp: Long,
    val lockTimestamp: Long? = null,
    val durationSeconds: Long = 0,
    val dateString: String // YYYY-MM-DD for fast daily aggregation
)
