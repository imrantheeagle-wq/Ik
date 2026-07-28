package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UnlockSessionEntity): Long

    @Update
    suspend fun updateSession(session: UnlockSessionEntity)

    @Query("SELECT * FROM unlock_sessions WHERE dateString = :dateStr ORDER BY unlockTimestamp DESC")
    fun getSessionsForDate(dateStr: String): Flow<List<UnlockSessionEntity>>

    @Query("SELECT SUM(durationSeconds) FROM unlock_sessions WHERE dateString = :dateStr")
    fun getTotalSecondsForDate(dateStr: String): Flow<Long?>

    @Query("SELECT COUNT(*) FROM unlock_sessions WHERE dateString = :dateStr")
    fun getUnlockCountForDate(dateStr: String): Flow<Int>

    @Query("SELECT * FROM unlock_sessions ORDER BY unlockTimestamp DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<UnlockSessionEntity>>

    @Query("DELETE FROM unlock_sessions")
    suspend fun clearAll()
}
