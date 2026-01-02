package com.incremax.domain.model

import java.time.LocalDate

enum class IncrementFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
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
    val completedDate: LocalDate? = null
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
    val pushUp100Challenge = WorkoutPlan(
        id = "preset_pushup_100",
        name = "100 Push-up Challenge",
        description = "Start with 10 push-ups and work your way up to 100 daily",
        exerciseId = "push_ups",
        startingAmount = 10,
        targetAmount = 100,
        incrementAmount = 2,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true
    )

    val squatMaster = WorkoutPlan(
        id = "preset_squat_master",
        name = "Squat Master",
        description = "Build leg strength with progressive squats",
        exerciseId = "squats",
        startingAmount = 15,
        targetAmount = 100,
        incrementAmount = 5,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true
    )

    val plankChallenge = WorkoutPlan(
        id = "preset_plank",
        name = "Plank Challenge",
        description = "Hold planks longer each week - from 30s to 5 minutes",
        exerciseId = "plank",
        startingAmount = 30,
        targetAmount = 300,
        incrementAmount = 15,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true
    )

    val couchTo5K = WorkoutPlan(
        id = "preset_couch_5k",
        name = "Couch to 5K",
        description = "Start walking, end up running 5 kilometers",
        exerciseId = "running",
        startingAmount = 500,
        targetAmount = 5000,
        incrementAmount = 250,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true
    )

    val sitUpSurge = WorkoutPlan(
        id = "preset_situp_surge",
        name = "Sit-up Surge",
        description = "Core strength builder - reach 75 daily sit-ups",
        exerciseId = "sit_ups",
        startingAmount = 10,
        targetAmount = 75,
        incrementAmount = 5,
        incrementFrequency = IncrementFrequency.WEEKLY,
        startDate = LocalDate.now(),
        isPreset = true
    )

    val all = listOf(
        pushUp100Challenge, squatMaster, plankChallenge, couchTo5K, sitUpSurge
    )
}
