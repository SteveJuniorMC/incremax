package com.incremax.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class WorkoutSession(
    val id: String,
    val planId: String,
    val exerciseId: String,
    val date: LocalDate,
    val completedAmount: Int,
    val targetAmount: Int,
    val xpEarned: Int,
    val durationSeconds: Long,
    val completedAt: LocalDateTime
) {
    val isCompleted: Boolean
        get() = completedAmount >= targetAmount

    val completionPercentage: Float
        get() = (completedAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
}
