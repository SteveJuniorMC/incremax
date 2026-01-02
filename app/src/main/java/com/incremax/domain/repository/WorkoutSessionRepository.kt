package com.incremax.domain.repository

import com.incremax.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutSessionRepository {
    fun getAllSessions(): Flow<List<WorkoutSession>>
    fun getSessionsByPlan(planId: String): Flow<List<WorkoutSession>>
    fun getSessionsByDate(date: LocalDate): Flow<List<WorkoutSession>>
    suspend fun getSessionsByDateSync(date: LocalDate): List<WorkoutSession>
    fun getSessionsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutSession>>
    suspend fun getSessionById(id: String): WorkoutSession?
    suspend fun getSessionForPlanOnDate(planId: String, date: LocalDate): WorkoutSession?
    suspend fun insertSession(session: WorkoutSession)
    suspend fun updateSession(session: WorkoutSession)
    suspend fun deleteSession(session: WorkoutSession)
    suspend fun getTotalSessionCount(): Int
    suspend fun getTotalXpEarned(): Int
    fun getWorkoutDates(): Flow<List<LocalDate>>
    suspend fun getUniqueDaysWorkedOut(): Int
    suspend fun getPersonalRecord(exerciseId: String): WorkoutSession?
    suspend fun getTotalForExercise(exerciseId: String): Long
}
