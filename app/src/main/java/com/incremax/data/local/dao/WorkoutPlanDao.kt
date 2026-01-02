package com.incremax.data.local.dao

import androidx.room.*
import com.incremax.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans")
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1")
    fun getActivePlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 0 AND completedDate IS NOT NULL")
    fun getCompletedPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlanById(id: String): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    fun getPlanByIdFlow(id: String): Flow<WorkoutPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<WorkoutPlanEntity>)

    @Update
    suspend fun updatePlan(plan: WorkoutPlanEntity)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlanEntity)

    @Query("DELETE FROM workout_plans WHERE id = :id")
    suspend fun deletePlanById(id: String)

    @Query("UPDATE workout_plans SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM workout_plans WHERE isActive = 0 AND completedDate IS NOT NULL")
    suspend fun getCompletedPlansCount(): Int

    @Query("UPDATE workout_plans SET reminderEnabled = :enabled, reminderTime = :time WHERE id = :id")
    suspend fun updateReminder(id: String, enabled: Boolean, time: String?)

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 AND reminderEnabled = 1")
    fun getPlansWithReminders(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 AND reminderEnabled = 1")
    suspend fun getPlansWithRemindersSync(): List<WorkoutPlanEntity>
}
