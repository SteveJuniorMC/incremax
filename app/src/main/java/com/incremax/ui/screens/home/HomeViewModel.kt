package com.incremax.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayWorkout(
    val plan: WorkoutPlan,
    val exercise: Exercise,
    val targetAmount: Int,
    val isCompleted: Boolean
)

data class HomeUiState(
    val userStats: UserStats = UserStats(),
    val todayWorkouts: List<TodayWorkout> = emptyList(),
    val isLoading: Boolean = true,
    val motivationalQuote: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val quotes = listOf(
        "Small steps lead to big results.",
        "Consistency beats intensity.",
        "Every rep counts towards your goal.",
        "You're stronger than you think.",
        "Progress, not perfection.",
        "The only bad workout is the one that didn't happen.",
        "Your future self will thank you.",
        "One day or day one. You decide.",
        "Discipline is choosing between what you want now and what you want most.",
        "It's not about being the best. It's about being better than yesterday."
    )

    init {
        viewModelScope.launch {
            initializeData()
            loadData()
        }
    }

    private suspend fun initializeData() {
        userStatsRepository.initializeStats()
        // Seed preset exercises if needed
        val exercises = exerciseRepository.getAllExercises().first()
        if (exercises.isEmpty()) {
            PresetExercises.all.forEach { exercise ->
                exerciseRepository.insertExercise(exercise)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userStatsRepository.getUserStats(),
                workoutPlanRepository.getActivePlans(),
                workoutSessionRepository.getSessionsByDate(LocalDate.now())
            ) { stats, plans, todaySessions ->
                val todayWorkouts = plans.mapNotNull { plan ->
                    val exercise = exerciseRepository.getExerciseById(plan.exerciseId) ?: return@mapNotNull null
                    val targetAmount = plan.getCurrentTarget(LocalDate.now())
                    val isCompleted = todaySessions.any { it.planId == plan.id && it.isCompleted }
                    TodayWorkout(plan, exercise, targetAmount, isCompleted)
                }

                HomeUiState(
                    userStats = stats,
                    todayWorkouts = todayWorkouts,
                    isLoading = false,
                    motivationalQuote = quotes.random()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}
