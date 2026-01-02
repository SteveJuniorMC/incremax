package com.incremax.domain.model

enum class FitnessGoal(
    val displayName: String,
    val description: String
) {
    STRENGTH(
        displayName = "Build Strength",
        description = "Increase muscle strength with bodyweight exercises"
    ),
    ENDURANCE(
        displayName = "Improve Endurance",
        description = "Build stamina and cardiovascular fitness"
    ),
    FLEXIBILITY(
        displayName = "Increase Flexibility",
        description = "Improve core stability and mobility"
    ),
    GENERAL_FITNESS(
        displayName = "General Fitness",
        description = "A balanced approach to overall health"
    )
}
