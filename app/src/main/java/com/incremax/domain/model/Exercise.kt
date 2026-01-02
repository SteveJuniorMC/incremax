package com.incremax.domain.model

enum class ExerciseType {
    REPS,       // Push-ups, squats, etc.
    TIME,       // Planks, stretches (seconds)
    DISTANCE    // Running, walking (meters)
}

enum class ExerciseCategory {
    BODYWEIGHT,
    CARDIO,
    FLEXIBILITY,
    CUSTOM
}

data class Exercise(
    val id: String,
    val name: String,
    val type: ExerciseType,
    val category: ExerciseCategory,
    val unit: String,
    val icon: String,
    val description: String,
    val isCustom: Boolean = false
)

object PresetExercises {
    val pushUps = Exercise(
        id = "push_ups",
        name = "Push-ups",
        type = ExerciseType.REPS,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "reps",
        icon = "fitness_center",
        description = "Classic upper body exercise"
    )

    val sitUps = Exercise(
        id = "sit_ups",
        name = "Sit-ups",
        type = ExerciseType.REPS,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "reps",
        icon = "self_improvement",
        description = "Core strengthening exercise"
    )

    val squats = Exercise(
        id = "squats",
        name = "Squats",
        type = ExerciseType.REPS,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "reps",
        icon = "directions_walk",
        description = "Lower body compound exercise"
    )

    val lunges = Exercise(
        id = "lunges",
        name = "Lunges",
        type = ExerciseType.REPS,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "reps",
        icon = "directions_walk",
        description = "Lower body exercise for legs and glutes"
    )

    val burpees = Exercise(
        id = "burpees",
        name = "Burpees",
        type = ExerciseType.REPS,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "reps",
        icon = "sports_gymnastics",
        description = "Full body high-intensity exercise"
    )

    val plank = Exercise(
        id = "plank",
        name = "Plank",
        type = ExerciseType.TIME,
        category = ExerciseCategory.BODYWEIGHT,
        unit = "seconds",
        icon = "accessibility_new",
        description = "Core stabilization exercise"
    )

    val jumpingJacks = Exercise(
        id = "jumping_jacks",
        name = "Jumping Jacks",
        type = ExerciseType.REPS,
        category = ExerciseCategory.CARDIO,
        unit = "reps",
        icon = "sports",
        description = "Cardio warm-up exercise"
    )

    val running = Exercise(
        id = "running",
        name = "Running",
        type = ExerciseType.DISTANCE,
        category = ExerciseCategory.CARDIO,
        unit = "meters",
        icon = "directions_run",
        description = "Cardiovascular endurance exercise"
    )

    val walking = Exercise(
        id = "walking",
        name = "Walking",
        type = ExerciseType.DISTANCE,
        category = ExerciseCategory.CARDIO,
        unit = "meters",
        icon = "directions_walk",
        description = "Low-impact cardio exercise"
    )

    val stretching = Exercise(
        id = "stretching",
        name = "Stretching",
        type = ExerciseType.TIME,
        category = ExerciseCategory.FLEXIBILITY,
        unit = "seconds",
        icon = "self_improvement",
        description = "Flexibility and recovery"
    )

    val all = listOf(
        pushUps, sitUps, squats, lunges, burpees,
        plank, jumpingJacks, running, walking, stretching
    )
}
