package com.incremax.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldNavigateToHome) {
        if (uiState.shouldNavigateToHome) {
            onComplete()
        }
    }

    AnimatedContent(
        targetState = uiState.currentStep,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) {
                // Moving forward
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            } else {
                // Moving backward
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
            direction
        },
        label = "onboarding_transition"
    ) { step ->
        when (step) {
            OnboardingStep.WELCOME -> WelcomeScreen(
                onContinue = { viewModel.navigateToStep(OnboardingStep.GOAL_SELECTION) },
                onSkip = { viewModel.skipOnboarding() }
            )

            OnboardingStep.GOAL_SELECTION -> GoalSelectionScreen(
                selectedGoal = uiState.selectedGoal,
                onGoalSelected = { viewModel.selectGoal(it) },
                onContinue = { viewModel.navigateToStep(OnboardingStep.PLAN_RECOMMENDATION) },
                onBack = { viewModel.navigateToStep(OnboardingStep.WELCOME) }
            )

            OnboardingStep.PLAN_RECOMMENDATION -> PlanRecommendationScreen(
                recommendedPlans = uiState.recommendedPlans,
                otherPlans = uiState.otherPlans,
                selectedPlanIds = uiState.selectedPlanIds,
                goalName = uiState.selectedGoal?.displayName ?: "",
                onTogglePlan = { viewModel.togglePlanSelection(it) },
                onContinue = { viewModel.navigateToStep(OnboardingStep.REMINDER_SETUP) },
                onBack = { viewModel.navigateToStep(OnboardingStep.GOAL_SELECTION) }
            )

            OnboardingStep.REMINDER_SETUP -> ReminderSetupScreen(
                selectedTime = uiState.reminderTime,
                onTimeSelected = { viewModel.setReminderTime(it) },
                onSetReminder = {
                    viewModel.enableReminder()
                    viewModel.navigateToStep(OnboardingStep.ACCOUNT_SUGGESTION)
                },
                onSkip = {
                    viewModel.skipReminder()
                    viewModel.navigateToStep(OnboardingStep.ACCOUNT_SUGGESTION)
                },
                onBack = { viewModel.navigateToStep(OnboardingStep.PLAN_RECOMMENDATION) }
            )

            OnboardingStep.ACCOUNT_SUGGESTION -> AccountSuggestionScreen(
                isLoading = uiState.isLoading,
                onCreateAccount = { /* Future: implement account creation */ },
                onSkip = { viewModel.completeOnboarding() },
                onBack = { viewModel.navigateToStep(OnboardingStep.REMINDER_SETUP) }
            )
        }
    }
}
