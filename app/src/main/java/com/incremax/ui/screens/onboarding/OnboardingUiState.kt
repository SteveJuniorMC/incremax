package com.incremax.ui.screens.onboarding

import com.incremax.domain.model.DifficultyLevel
import com.incremax.domain.model.WorkoutPlan
import java.time.LocalTime

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedLevel: DifficultyLevel? = null,
    val recommendedPlans: List<WorkoutPlan> = emptyList(),
    val selectedPlanIds: Set<String> = emptySet(),
    val reminderTime: LocalTime = LocalTime.of(9, 0),
    val reminderEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val shouldNavigateToHome: Boolean = false
)

enum class OnboardingStep {
    WELCOME,
    FITNESS_LEVEL,
    PLAN_RECOMMENDATION,
    REMINDER_SETUP,
    ACCOUNT_SUGGESTION
}
