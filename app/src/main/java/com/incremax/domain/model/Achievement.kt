package com.incremax.domain.model

import java.time.LocalDateTime

enum class AchievementCategory {
    STREAK,
    EXERCISE,
    LEVEL,
    SPECIAL
}

sealed class AchievementRequirement {
    data class StreakDays(val days: Int) : AchievementRequirement()
    data class TotalExerciseCount(val exerciseId: String, val count: Int) : AchievementRequirement()
    data class TotalWorkouts(val count: Int) : AchievementRequirement()
    data class ReachLevel(val level: Int) : AchievementRequirement()
    data class CompletePlans(val count: Int) : AchievementRequirement()
    data class TotalXp(val xp: Int) : AchievementRequirement()
}

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val requirement: AchievementRequirement,
    val xpReward: Int,
    val isHidden: Boolean = false,
    val unlockedAt: LocalDateTime? = null
) {
    val isUnlocked: Boolean
        get() = unlockedAt != null
}

object PresetAchievements {
    // Streak Achievements
    val firstWorkout = Achievement(
        id = "first_workout",
        name = "First Step",
        description = "Complete your first workout",
        icon = "star",
        category = AchievementCategory.STREAK,
        requirement = AchievementRequirement.TotalWorkouts(1),
        xpReward = 50
    )

    val streak7 = Achievement(
        id = "streak_7",
        name = "Week Warrior",
        description = "Maintain a 7-day streak",
        icon = "local_fire_department",
        category = AchievementCategory.STREAK,
        requirement = AchievementRequirement.StreakDays(7),
        xpReward = 100
    )

    val streak30 = Achievement(
        id = "streak_30",
        name = "Monthly Master",
        description = "Maintain a 30-day streak",
        icon = "whatshot",
        category = AchievementCategory.STREAK,
        requirement = AchievementRequirement.StreakDays(30),
        xpReward = 300
    )

    val streak100 = Achievement(
        id = "streak_100",
        name = "Century Club",
        description = "Maintain a 100-day streak",
        icon = "military_tech",
        category = AchievementCategory.STREAK,
        requirement = AchievementRequirement.StreakDays(100),
        xpReward = 1000
    )

    val streak365 = Achievement(
        id = "streak_365",
        name = "Year of Iron",
        description = "Maintain a 365-day streak",
        icon = "emoji_events",
        category = AchievementCategory.STREAK,
        requirement = AchievementRequirement.StreakDays(365),
        xpReward = 5000
    )

    // Exercise Achievements
    val pushUp100 = Achievement(
        id = "pushup_100",
        name = "Push-up Beginner",
        description = "Complete 100 total push-ups",
        icon = "fitness_center",
        category = AchievementCategory.EXERCISE,
        requirement = AchievementRequirement.TotalExerciseCount("push_ups", 100),
        xpReward = 50
    )

    val pushUp1000 = Achievement(
        id = "pushup_1000",
        name = "Push-up Pro",
        description = "Complete 1,000 total push-ups",
        icon = "fitness_center",
        category = AchievementCategory.EXERCISE,
        requirement = AchievementRequirement.TotalExerciseCount("push_ups", 1000),
        xpReward = 200
    )

    val pushUp10000 = Achievement(
        id = "pushup_10000",
        name = "Push-up Legend",
        description = "Complete 10,000 total push-ups",
        icon = "fitness_center",
        category = AchievementCategory.EXERCISE,
        requirement = AchievementRequirement.TotalExerciseCount("push_ups", 10000),
        xpReward = 1000
    )

    val squat1000 = Achievement(
        id = "squat_1000",
        name = "Squat Specialist",
        description = "Complete 1,000 total squats",
        icon = "directions_walk",
        category = AchievementCategory.EXERCISE,
        requirement = AchievementRequirement.TotalExerciseCount("squats", 1000),
        xpReward = 200
    )

    val plank1Hour = Achievement(
        id = "plank_1hour",
        name = "Iron Core",
        description = "Hold planks for a total of 1 hour",
        icon = "accessibility_new",
        category = AchievementCategory.EXERCISE,
        requirement = AchievementRequirement.TotalExerciseCount("plank", 3600),
        xpReward = 300
    )

    // Level Achievements
    val level5 = Achievement(
        id = "level_5",
        name = "Rising Star",
        description = "Reach level 5",
        icon = "grade",
        category = AchievementCategory.LEVEL,
        requirement = AchievementRequirement.ReachLevel(5),
        xpReward = 100
    )

    val level10 = Achievement(
        id = "level_10",
        name = "Dedicated Athlete",
        description = "Reach level 10",
        icon = "workspace_premium",
        category = AchievementCategory.LEVEL,
        requirement = AchievementRequirement.ReachLevel(10),
        xpReward = 250
    )

    val level20 = Achievement(
        id = "level_20",
        name = "Fitness Master",
        description = "Reach level 20",
        icon = "emoji_events",
        category = AchievementCategory.LEVEL,
        requirement = AchievementRequirement.ReachLevel(20),
        xpReward = 500
    )

    // Special Achievements
    val completePlan = Achievement(
        id = "complete_plan",
        name = "Plan Completed",
        description = "Complete your first workout plan",
        icon = "check_circle",
        category = AchievementCategory.SPECIAL,
        requirement = AchievementRequirement.CompletePlans(1),
        xpReward = 200
    )

    val complete5Plans = Achievement(
        id = "complete_5_plans",
        name = "Goal Crusher",
        description = "Complete 5 workout plans",
        icon = "verified",
        category = AchievementCategory.SPECIAL,
        requirement = AchievementRequirement.CompletePlans(5),
        xpReward = 500
    )

    val xp10000 = Achievement(
        id = "xp_10000",
        name = "XP Hunter",
        description = "Earn 10,000 total XP",
        icon = "stars",
        category = AchievementCategory.SPECIAL,
        requirement = AchievementRequirement.TotalXp(10000),
        xpReward = 200
    )

    val all = listOf(
        firstWorkout, streak7, streak30, streak100, streak365,
        pushUp100, pushUp1000, pushUp10000, squat1000, plank1Hour,
        level5, level10, level20,
        completePlan, complete5Plans, xp10000
    )
}
