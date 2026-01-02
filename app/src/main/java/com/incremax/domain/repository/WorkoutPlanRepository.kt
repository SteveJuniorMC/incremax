package com.incremax.domain.repository

import com.incremax.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface WorkoutPlanRepository {
    fun getAllPlans(): Flow<List<WorkoutPlan>>
    fun getActivePlans(): Flow<List<WorkoutPlan>>
    fun getCompletedPlans(): Flow<List<WorkoutPlan>>
    suspend fun getPlanById(id: String): WorkoutPlan?
    fun getPlanByIdFlow(id: String): Flow<WorkoutPlan?>
    suspend fun insertPlan(plan: WorkoutPlan)
    suspend fun updatePlan(plan: WorkoutPlan)
    suspend fun deletePlan(id: String)
    suspend fun setActive(id: String, isActive: Boolean)
    suspend fun getCompletedPlansCount(): Int
    suspend fun updateReminder(id: String, enabled: Boolean, time: LocalTime?)
    fun getPlansWithReminders(): Flow<List<WorkoutPlan>>
    suspend fun getPlansWithRemindersSync(): List<WorkoutPlan>
}
