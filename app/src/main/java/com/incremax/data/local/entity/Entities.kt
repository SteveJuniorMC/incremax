package com.incremax.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.incremax.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: ExerciseType,
    val category: ExerciseCategory,
    val unit: String,
    val icon: String,
    val description: String,
    val isCustom: Boolean
) {
    fun toDomain() = Exercise(
        id = id,
        name = name,
        type = type,
        category = category,
        unit = unit,
        icon = icon,
        description = description,
        isCustom = isCustom
    )

    companion object {
        fun fromDomain(exercise: Exercise) = ExerciseEntity(
            id = exercise.id,
            name = exercise.name,
            type = exercise.type,
            category = exercise.category,
            unit = exercise.unit,
            icon = exercise.icon,
            description = exercise.description,
            isCustom = exercise.isCustom
        )
    }
}

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val exerciseId: String,
    val startingAmount: Int,
    val targetAmount: Int,
    val incrementAmount: Int,
    val incrementFrequency: IncrementFrequency,
    val startDate: LocalDate,
    val isActive: Boolean,
    val isPreset: Boolean,
    val completedDate: LocalDate?
) {
    fun toDomain() = WorkoutPlan(
        id = id,
        name = name,
        description = description,
        exerciseId = exerciseId,
        startingAmount = startingAmount,
        targetAmount = targetAmount,
        incrementAmount = incrementAmount,
        incrementFrequency = incrementFrequency,
        startDate = startDate,
        isActive = isActive,
        isPreset = isPreset,
        completedDate = completedDate
    )

    companion object {
        fun fromDomain(plan: WorkoutPlan) = WorkoutPlanEntity(
            id = plan.id,
            name = plan.name,
            description = plan.description,
            exerciseId = plan.exerciseId,
            startingAmount = plan.startingAmount,
            targetAmount = plan.targetAmount,
            incrementAmount = plan.incrementAmount,
            incrementFrequency = plan.incrementFrequency,
            startDate = plan.startDate,
            isActive = plan.isActive,
            isPreset = plan.isPreset,
            completedDate = plan.completedDate
        )
    }
}

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val exerciseId: String,
    val date: LocalDate,
    val completedAmount: Int,
    val targetAmount: Int,
    val xpEarned: Int,
    val durationSeconds: Long,
    val completedAt: LocalDateTime
) {
    fun toDomain() = WorkoutSession(
        id = id,
        planId = planId,
        exerciseId = exerciseId,
        date = date,
        completedAmount = completedAmount,
        targetAmount = targetAmount,
        xpEarned = xpEarned,
        durationSeconds = durationSeconds,
        completedAt = completedAt
    )

    companion object {
        fun fromDomain(session: WorkoutSession) = WorkoutSessionEntity(
            id = session.id,
            planId = session.planId,
            exerciseId = session.exerciseId,
            date = session.date,
            completedAmount = session.completedAmount,
            targetAmount = session.targetAmount,
            xpEarned = session.xpEarned,
            durationSeconds = session.durationSeconds,
            completedAt = session.completedAt
        )
    }
}

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalWorkouts: Int,
    val streakFreezes: Int,
    val lastWorkoutDate: LocalDate?
) {
    fun toDomain() = UserStats(
        totalXp = totalXp,
        level = level,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalWorkouts = totalWorkouts,
        streakFreezes = streakFreezes,
        lastWorkoutDate = lastWorkoutDate
    )

    companion object {
        fun fromDomain(stats: UserStats) = UserStatsEntity(
            totalXp = stats.totalXp,
            level = stats.level,
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak,
            totalWorkouts = stats.totalWorkouts,
            streakFreezes = stats.streakFreezes,
            lastWorkoutDate = stats.lastWorkoutDate
        )
    }
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: LocalDateTime?
)

@Entity(tableName = "exercise_totals")
data class ExerciseTotalEntity(
    @PrimaryKey val exerciseId: String,
    val totalAmount: Long
)
