package com.incremax.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.incremax.ui.screens.auth.SignInScreen

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
                onContinue = { viewModel.navigateToStep(OnboardingStep.FITNESS_LEVEL) },
                onSkip = { viewModel.skipOnboarding() }
            )

            OnboardingStep.FITNESS_LEVEL -> FitnessLevelScreen(
                selectedLevel = uiState.selectedLevel,
                onLevelSelected = { viewModel.selectFitnessLevel(it) },
                onShowAll = {
                    viewModel.showAllChallenges()
                    viewModel.navigateToStep(OnboardingStep.PLAN_RECOMMENDATION)
                },
                onContinue = { viewModel.navigateToStep(OnboardingStep.PLAN_RECOMMENDATION) },
                onBack = { viewModel.navigateToStep(OnboardingStep.WELCOME) }
            )

            OnboardingStep.PLAN_RECOMMENDATION -> PlanRecommendationScreen(
                recommendedPlans = uiState.recommendedPlans,
                selectedPlanIds = uiState.selectedPlanIds,
                levelName = uiState.selectedLevel?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All",
                onTogglePlan = { viewModel.togglePlanSelection(it) },
                onContinue = { viewModel.navigateToStep(OnboardingStep.REMINDER_SETUP) },
                onBack = { viewModel.navigateToStep(OnboardingStep.FITNESS_LEVEL) }
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

            OnboardingStep.ACCOUNT_SUGGESTION -> SignInScreen(
                onSignInComplete = { viewModel.completeOnboarding() },
                onSkip = { viewModel.completeOnboarding() },
                onBack = { viewModel.navigateToStep(OnboardingStep.REMINDER_SETUP) },
                isOnboarding = true
            )
        }
    }
}
