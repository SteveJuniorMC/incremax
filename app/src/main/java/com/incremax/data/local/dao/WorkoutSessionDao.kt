package com.incremax.data.local.dao

import androidx.room.*
import com.incremax.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions")
    suspend fun getAllSessionsSync(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE planId = :planId ORDER BY completedAt DESC")
    fun getSessionsByPlan(planId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date = :date")
    fun getSessionsByDate(date: LocalDate): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date = :date")
    suspend fun getSessionsByDateSync(date: LocalDate): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSessionsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE planId = :planId AND date = :date LIMIT 1")
    suspend fun getSessionForPlanOnDate(planId: String, date: LocalDate): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getTotalSessionCount(): Int

    @Query("SELECT SUM(xpEarned) FROM workout_sessions")
    suspend fun getTotalXpEarned(): Int?

    @Query("SELECT DISTINCT date FROM workout_sessions ORDER BY date DESC")
    fun getWorkoutDates(): Flow<List<LocalDate>>

    @Query("SELECT COUNT(DISTINCT date) FROM workout_sessions")
    suspend fun getUniqueDaysWorkedOut(): Int

    @Query("""
        SELECT * FROM workout_sessions
        WHERE exerciseId = :exerciseId
        ORDER BY completedAmount DESC
        LIMIT 1
    """)
    suspend fun getPersonalRecord(exerciseId: String): WorkoutSessionEntity?

    @Query("SELECT SUM(completedAmount) FROM workout_sessions WHERE exerciseId = :exerciseId")
    suspend fun getTotalForExercise(exerciseId: String): Long?

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}
