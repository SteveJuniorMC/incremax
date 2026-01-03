package com.incremax.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class IncrementFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val exerciseId: String,
    val startingAmount: Int,
    val targetAmount: Int,
    val incrementAmount: Int,
    val incrementFrequency: IncrementFrequency,
    val startDate: LocalDate,
    val isActive: Boolean = true,
    val isPreset: Boolean = false,
    val completedDate: LocalDate? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime? = null,
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER
) {
    fun getCurrentTarget(currentDate: LocalDate): Int {
        val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDate).toInt()
        if (daysSinceStart < 0) return startingAmount

        val periods = when (incrementFrequency) {
            IncrementFrequency.DAILY -> daysSinceStart
            IncrementFrequency.WEEKLY -> daysSinceStart / 7
            IncrementFrequency.BIWEEKLY -> daysSinceStart / 14
            IncrementFrequency.MONTHLY -> daysSinceStart / 30
        }

        val calculatedTarget = startingAmount + (periods * incrementAmount)
        return minOf(calculatedTarget, targetAmount)
    }

    fun getProgressPercentage(currentDate: LocalDate): Float {
        val current = getCurrentTarget(currentDate)
        return ((current - startingAmount).toFloat() / (targetAmount - startingAmount).toFloat()).coerceIn(0f, 1f)
    }

    fun getDaysUntilTarget(currentDate: LocalDate): Int {
        val current = getCurrentTarget(currentDate)
        if (current >= targetAmount) return 0

        val remaining = targetAmount - current
        val incrementsNeeded = (remaining + incrementAmount - 1) / incrementAmount

        return when (incrementFrequency) {
            IncrementFrequency.DAILY -> incrementsNeeded
            IncrementFrequency.WEEKLY -> incrementsNeeded * 7
            IncrementFrequency.BIWEEKLY -> incrementsNeeded * 14
            IncrementFrequency.MONTHLY -> incrementsNeeded * 30
        }
    }
}

object PresetPlans {
    // ============== BEGINNER CHALLENGES ==============
    // Easy entry point - start embarrassingly small

    val firstPushUps = WorkoutPlan(
        id = "preset_beginner_pushups",
        name = "First Push-ups",
        description = "Your push-up journey starts here",
        exerciseId = "push_ups",
        startingAmount = 1,
        targetAmount = 20,
        incrementAmount = 1,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.BEGINNER
    )

    val squatStarter = WorkoutPlan(
        id = "preset_beginner_squats",
        name = "Squat Starter",
        description = "Build a foundation for strong legs",
        exerciseId = "squats",
        startingAmount = 5,
        targetAmount = 30,
        incrementAmount = 2,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.BEGINNER
    )

    val coreFoundations = WorkoutPlan(
        id = "preset_beginner_situps",
        name = "Core Foundations",
        description = "Gentle core strengthening",
        exerciseId = "sit_ups",
        startingAmount = 3,
        targetAmount = 25,
        incrementAmount = 2,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.BEGINNER
    )

    // ============== INTERMEDIATE CHALLENGES ==============
    // For those with some fitness base

    val pushUpBuilder = WorkoutPlan(
        id = "preset_intermediate_pushups",
        name = "Push-up Builder",
        description = "Take your push-ups to the next level",
        exerciseId = "push_ups",
        startingAmount = 15,
        targetAmount = 50,
        incrementAmount = 3,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.INTERMEDIATE
    )

    val squatStrength = WorkoutPlan(
        id = "preset_intermediate_squats",
        name = "Squat Strength",
        description = "Build serious leg power",
        exerciseId = "squats",
        startingAmount = 25,
        targetAmount = 75,
        incrementAmount = 5,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.INTERMEDIATE
    )

    val corePower = WorkoutPlan(
        id = "preset_intermediate_situps",
        name = "Core Power",
        description = "Strengthen your entire core",
        exerciseId = "sit_ups",
        startingAmount = 20,
        targetAmount = 60,
        incrementAmount = 4,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.INTERMEDIATE
    )

    // ============== ADVANCED CHALLENGES ==============
    // For fit individuals pushing their limits

    val pushUp100Challenge = WorkoutPlan(
        id = "preset_advanced_pushups",
        name = "100 Push-up Challenge",
        description = "The ultimate push-up goal",
        exerciseId = "push_ups",
        startingAmount = 30,
        targetAmount = 100,
        incrementAmount = 5,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.ADVANCED
    )

    val squatMaster = WorkoutPlan(
        id = "preset_advanced_squats",
        name = "Squat Master",
        description = "Achieve legendary leg strength",
        exerciseId = "squats",
        startingAmount = 50,
        targetAmount = 150,
        incrementAmount = 10,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.ADVANCED
    )

    val ironCore = WorkoutPlan(
        id = "preset_advanced_situps",
        name = "Iron Core",
        description = "Build an unbreakable core",
        exerciseId = "sit_ups",
        startingAmount = 40,
        targetAmount = 100,
        incrementAmount = 5,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true,
        difficulty = DifficultyLevel.ADVANCED
    )

    // Grouped by difficulty
    val beginner = listOf(firstPushUps, squatStarter, coreFoundations)
    val intermediate = listOf(pushUpBuilder, squatStrength, corePower)
    val advanced = listOf(pushUp100Challenge, squatMaster, ironCore)

    val all = beginner + intermediate + advanced

    fun forDifficulty(level: DifficultyLevel): List<WorkoutPlan> = when (level) {
        DifficultyLevel.BEGINNER -> beginner
        DifficultyLevel.INTERMEDIATE -> intermediate
        DifficultyLevel.ADVANCED -> advanced
    }
}
