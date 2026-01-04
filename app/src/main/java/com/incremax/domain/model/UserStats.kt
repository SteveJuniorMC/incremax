package com.incremax.domain.model

import java.time.LocalDate

data class UserStats(
    val totalXp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val streakFreezes: Int = 0,
    val lastWorkoutDate: LocalDate? = null
) {
    companion object {
        private val levelThresholds = listOf(
            0, 300, 700, 1200, 2000, 3000, 4500, 6500, 9000, 12000,
            16000, 21000, 28000, 37000, 48000, 62000, 80000, 105000, 140000, 185000
        )

        private val levelTitles = listOf(
            "Starter", "Beginner", "Novice", "Enthusiast", "Apprentice",
            "Fighter", "Athlete", "Warrior", "Gladiator", "Contender",
            "Master", "Grandmaster", "Champion", "Hero", "Legend",
            "Paragon", "Demigod", "Titan", "Immortal", "Olympian"
        )

        fun calculateLevel(xp: Int): Int {
            return levelThresholds.indexOfLast { xp >= it }.coerceAtLeast(0) + 1
        }

        fun xpForLevel(level: Int): Int {
            return levelThresholds.getOrElse(level - 1) { levelThresholds.last() }
        }

        fun xpForNextLevel(level: Int): Int {
            return levelThresholds.getOrElse(level) { levelThresholds.last() }
        }

        fun getLevelTitle(level: Int): String {
            return levelTitles.getOrElse(level - 1) { levelTitles.last() }
        }
    }

    val levelTitle: String
        get() = getLevelTitle(level)

    val xpForCurrentLevel: Int
        get() = xpForLevel(level)

    val xpForNextLevel: Int
        get() = xpForNextLevel(level)

    val xpProgress: Float
        get() {
            val currentLevelXp = xpForCurrentLevel
            val nextLevelXp = xpForNextLevel
            val xpIntoLevel = totalXp - currentLevelXp
            val xpNeeded = nextLevelXp - currentLevelXp
            return (xpIntoLevel.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)
        }

    val xpToNextLevel: Int
        get() = xpForNextLevel - totalXp

    fun isStreakActive(today: LocalDate): Boolean {
        if (lastWorkoutDate == null) return false
        val daysSinceLastWorkout = java.time.temporal.ChronoUnit.DAYS.between(lastWorkoutDate, today)
        return daysSinceLastWorkout <= 1
    }
}

object XpRewards {
    const val BASE_WORKOUT_XP = 50
    const val STREAK_BONUS_PER_DAY = 5
    const val MAX_STREAK_BONUS = 50
    const val PERFECT_WORKOUT_BONUS = 25
    const val PLAN_COMPLETION_BONUS = 200
    const val ACHIEVEMENT_XP_MULTIPLIER = 1

    fun calculateWorkoutXp(
        streakDays: Int,
        isPerfect: Boolean = false
    ): Int {
        val streakBonus = minOf(streakDays * STREAK_BONUS_PER_DAY, MAX_STREAK_BONUS)
        val perfectBonus = if (isPerfect) PERFECT_WORKOUT_BONUS else 0
        return BASE_WORKOUT_XP + streakBonus + perfectBonus
    }
}
