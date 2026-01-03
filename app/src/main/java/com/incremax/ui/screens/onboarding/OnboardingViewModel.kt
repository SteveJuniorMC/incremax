package com.incremax.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.FitnessGoal
import com.incremax.domain.model.PresetPlans
import com.incremax.domain.model.WorkoutPlan
import com.incremax.domain.repository.OnboardingRepository
import com.incremax.domain.repository.WorkoutPlanRepository
import com.incremax.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectGoal(goal: FitnessGoal) {
        val (recommended, other) = getPlansForGoal(goal)
        _uiState.update {
            it.copy(
                selectedGoal = goal,
                recommendedPlans = recommended,
                otherPlans = other,
                selectedPlanIds = recommended.map { plan -> plan.id }.toSet()
            )
        }
    }

    private fun getPlansForGoal(goal: FitnessGoal): Pair<List<WorkoutPlan>, List<WorkoutPlan>> {
        val allPlans = PresetPlans.all
        val recommended = when (goal) {
            FitnessGoal.STRENGTH -> listOf(
                PresetPlans.pushUp100Challenge,
                PresetPlans.squatMaster,
                PresetPlans.sitUpSurge
            )
            FitnessGoal.ENDURANCE -> listOf(
                PresetPlans.couchTo5K,
                PresetPlans.plankChallenge
            )
            FitnessGoal.FLEXIBILITY -> listOf(
                PresetPlans.plankChallenge
            )
            FitnessGoal.GENERAL_FITNESS -> allPlans.take(3)
        }
        val other = allPlans - recommended.toSet()
        return recommended to other
    }

    fun togglePlanSelection(planId: String) {
        _uiState.update {
            val newSelection = if (planId in it.selectedPlanIds) {
                it.selectedPlanIds - planId
            } else {
                it.selectedPlanIds + planId
            }
            it.copy(selectedPlanIds = newSelection)
        }
    }

    fun setReminderTime(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun enableReminder() {
        _uiState.update { it.copy(reminderEnabled = true) }
    }

    fun skipReminder() {
        _uiState.update { it.copy(reminderEnabled = false) }
    }

    fun navigateToStep(step: OnboardingStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    private var plansInserted = false

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Save selected goal
            _uiState.value.selectedGoal?.let { goal ->
                onboardingRepository.setSelectedGoal(goal)
            }

            // Only insert plans once, and skip if user already has plans (e.g., from cloud sync)
            if (!plansInserted) {
                plansInserted = true

                // Check if user already has plans (from cloud sync during sign-in)
                val existingPlans = workoutPlanRepository.getActivePlans().first()
                if (existingPlans.isEmpty()) {
                    // Activate selected plans
                    val today = LocalDate.now()
                    val reminderTime = if (_uiState.value.reminderEnabled) {
                        _uiState.value.reminderTime
                    } else null

                    val allPlans = _uiState.value.recommendedPlans + _uiState.value.otherPlans
                    val selectedPlans = allPlans.filter { it.id in _uiState.value.selectedPlanIds }

                    selectedPlans.forEach { presetPlan ->
                        val newPlan = presetPlan.copy(
                            id = UUID.randomUUID().toString(),
                            startDate = today,
                            isActive = true,
                            reminderEnabled = _uiState.value.reminderEnabled,
                            reminderTime = reminderTime
                        )
                        workoutPlanRepository.insertPlan(newPlan)

                        // Schedule reminder for this plan if enabled
                        if (_uiState.value.reminderEnabled) {
                            notificationScheduler.schedulePlanReminder(newPlan)
                        }
                    }
                }
            }

            // Mark onboarding as complete
            onboardingRepository.setOnboardingCompleted(true)

            _uiState.update {
                it.copy(isLoading = false, shouldNavigateToHome = true)
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingCompleted(true)
            _uiState.update { it.copy(shouldNavigateToHome = true) }
        }
    }
}
