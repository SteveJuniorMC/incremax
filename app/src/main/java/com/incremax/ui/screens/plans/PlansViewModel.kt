package com.incremax.ui.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.incremax.notification.NotificationScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class PlanWithExercise(
    val plan: WorkoutPlan,
    val exercise: Exercise,
    val currentTarget: Int,
    val progressPercentage: Float
)

data class ActivatedPlanInfo(
    val id: String,
    val name: String
)

data class PlansUiState(
    val activePlans: List<PlanWithExercise> = emptyList(),
    val completedPlans: List<PlanWithExercise> = emptyList(),
    val presetPlans: List<WorkoutPlan> = PresetPlans.all,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val showReminderPrompt: Boolean = false,
    val activatedPlan: ActivatedPlanInfo? = null
)

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                workoutPlanRepository.getActivePlans(),
                workoutPlanRepository.getCompletedPlans(),
                exerciseRepository.getAllExercises()
            ) { active, completed, exercises ->
                val today = LocalDate.now()

                val activePlansWithExercise = active.mapNotNull { plan ->
                    val exercise = exercises.find { it.id == plan.exerciseId } ?: return@mapNotNull null
                    PlanWithExercise(
                        plan = plan,
                        exercise = exercise,
                        currentTarget = plan.getCurrentTarget(today),
                        progressPercentage = plan.getProgressPercentage(today)
                    )
                }

                val completedPlansWithExercise = completed.mapNotNull { plan ->
                    val exercise = exercises.find { it.id == plan.exerciseId } ?: return@mapNotNull null
                    PlanWithExercise(
                        plan = plan,
                        exercise = exercise,
                        currentTarget = plan.targetAmount,
                        progressPercentage = 1f
                    )
                }

                PlansUiState(
                    activePlans = activePlansWithExercise,
                    completedPlans = completedPlansWithExercise,
                    exercises = exercises,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun activatePresetPlan(presetPlan: WorkoutPlan) {
        viewModelScope.launch {
            val planId = UUID.randomUUID().toString()
            val newPlan = presetPlan.copy(
                id = planId,
                startDate = LocalDate.now(),
                isActive = true
            )
            workoutPlanRepository.insertPlan(newPlan)
            _uiState.update {
                it.copy(
                    showReminderPrompt = true,
                    activatedPlan = ActivatedPlanInfo(id = planId, name = presetPlan.name),
                    selectedTab = 0
                )
            }
        }
    }

    fun setReminder(time: LocalTime) {
        val planInfo = _uiState.value.activatedPlan ?: return
        viewModelScope.launch {
            workoutPlanRepository.updateReminder(planInfo.id, true, time)
            notificationScheduler.scheduleAllPlanReminders()
            _uiState.update { it.copy(showReminderPrompt = false, activatedPlan = null) }
        }
    }

    fun skipReminder() {
        _uiState.update { it.copy(showReminderPrompt = false, activatedPlan = null) }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            workoutPlanRepository.deletePlan(planId)
        }
    }
}
