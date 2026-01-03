package com.incremax.ui.screens.workout

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
import com.incremax.ui.components.RewardScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class WorkoutUiState(
    val plan: WorkoutPlan? = null,
    val exercise: Exercise? = null,
    val targetAmount: Int = 0,
    val completedAmount: Int = 0,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val xpEarned: Int = 0,
    val elapsedSeconds: Long = 0,
    val showRewardScreen: Boolean = false,
    val totalXpBefore: Int = 0,
    val previousLevel: Int = 1,
    val newLevel: Int = 1,
    val currentStreak: Int = 0,
    val newAchievements: List<Achievement> = emptyList()
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _workoutComplete = MutableSharedFlow<Unit>()
    val workoutComplete: SharedFlow<Unit> = _workoutComplete.asSharedFlow()

    private var startTime: Long = 0

    fun loadWorkout(planId: String) {
        viewModelScope.launch {
            val plan = workoutPlanRepository.getPlanById(planId) ?: return@launch
            val exercise = exerciseRepository.getExerciseById(plan.exerciseId)
            val today = LocalDate.now()

            // Check if already completed today
            val existingSession = workoutSessionRepository.getSessionForPlanOnDate(planId, today)

            _uiState.value = WorkoutUiState(
                plan = plan,
                exercise = exercise,
                targetAmount = plan.getCurrentTarget(today),
                completedAmount = existingSession?.completedAmount ?: 0,
                isLoading = false,
                isCompleted = existingSession?.isCompleted == true
            )

            startTime = System.currentTimeMillis()
            startTimer()
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _uiState.update { it.copy(elapsedSeconds = elapsed) }
            }
        }
    }

    fun incrementAmount(by: Int = 1) {
        _uiState.update {
            val newAmount = (it.completedAmount + by).coerceAtMost(it.targetAmount * 2)
            it.copy(completedAmount = newAmount)
        }
    }

    fun decrementAmount(by: Int = 1) {
        _uiState.update {
            val newAmount = (it.completedAmount - by).coerceAtLeast(0)
            it.copy(completedAmount = newAmount)
        }
    }

    fun setAmount(amount: Int) {
        _uiState.update { it.copy(completedAmount = amount.coerceAtLeast(0)) }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            val state = _uiState.value
            val plan = state.plan ?: return@launch
            val today = LocalDate.now()

            // Get current stats before changes
            val userStatsBefore = userStatsRepository.getUserStatsSync()
            val previousLevel = userStatsBefore.level
            val totalXpBefore = userStatsBefore.totalXp
            val achievementsBefore = userStatsRepository.getUnlockedAchievementIds()

            // Calculate XP
            val isPerfect = state.completedAmount >= state.targetAmount
            val xpEarned = XpRewards.calculateWorkoutXp(
                streakDays = userStatsBefore.currentStreak,
                isPerfect = isPerfect
            )

            // Create session
            val session = WorkoutSession(
                id = UUID.randomUUID().toString(),
                planId = plan.id,
                exerciseId = plan.exerciseId,
                date = today,
                completedAmount = state.completedAmount,
                targetAmount = state.targetAmount,
                xpEarned = xpEarned,
                durationSeconds = state.elapsedSeconds,
                completedAt = LocalDateTime.now()
            )

            workoutSessionRepository.insertSession(session)

            // Update exercise totals
            exerciseRepository.addToExerciseTotal(plan.exerciseId, state.completedAmount)

            // Update user stats
            userStatsRepository.addXp(xpEarned)
            userStatsRepository.incrementWorkouts()

            // Update streak
            val lastWorkoutDate = userStatsBefore.lastWorkoutDate
            val newStreak = when {
                lastWorkoutDate == null -> 1
                lastWorkoutDate == today -> userStatsBefore.currentStreak
                lastWorkoutDate == today.minusDays(1) -> userStatsBefore.currentStreak + 1
                else -> 1
            }
            userStatsRepository.updateStreak(newStreak)
            userStatsRepository.updateLastWorkoutDate(today)

            // Check achievements
            userStatsRepository.checkAndUnlockAchievements()

            // Check if plan is complete
            if (state.completedAmount >= plan.targetAmount && plan.getCurrentTarget(today) >= plan.targetAmount) {
                val completedPlan = plan.copy(
                    isActive = false,
                    completedDate = today
                )
                workoutPlanRepository.updatePlan(completedPlan)
            }

            // Get updated stats
            val userStatsAfter = userStatsRepository.getUserStatsSync()
            val newLevel = userStatsAfter.level

            val achievementsAfter = userStatsRepository.getUnlockedAchievementIds()
            val newAchievementIds = achievementsAfter - achievementsBefore
            val newAchievements = if (newAchievementIds.isNotEmpty()) {
                userStatsRepository.getAchievementsByIds(newAchievementIds.toList())
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    isCompleted = true,
                    xpEarned = xpEarned,
                    showRewardScreen = true,
                    totalXpBefore = totalXpBefore,
                    previousLevel = previousLevel,
                    newLevel = newLevel,
                    currentStreak = newStreak,
                    newAchievements = newAchievements
                )
            }
        }
    }

    fun dismissRewardScreen() {
        _uiState.update { it.copy(showRewardScreen = false) }
        viewModelScope.launch {
            _workoutComplete.emit(Unit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    planId: String,
    onWorkoutComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(planId) {
        viewModel.loadWorkout(planId)
    }

    LaunchedEffect(Unit) {
        viewModel.workoutComplete.collect {
            onWorkoutComplete()
        }
    }

    // Reward Screen (Duolingo-style)
    if (uiState.showRewardScreen) {
        RewardScreen(
            xpEarned = uiState.xpEarned,
            totalXpBefore = uiState.totalXpBefore,
            previousLevel = uiState.previousLevel,
            newLevel = uiState.newLevel,
            currentStreak = uiState.currentStreak,
            newAchievements = uiState.newAchievements,
            onDismiss = { viewModel.dismissRewardScreen() }
        )
        return // Don't show workout screen behind it
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Timer
                    Text(
                        text = formatTime(uiState.elapsedSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Plan info
                Text(
                    text = uiState.plan?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Main counter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Progress ring
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = (uiState.completedAmount.toFloat() / uiState.targetAmount).coerceIn(0f, 1f)

                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(200.dp),
                            strokeWidth = 12.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.completedAmount}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp
                            )
                            Text(
                                text = "/ ${uiState.targetAmount} ${uiState.exercise?.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Counter controls with haptic feedback
                    val view = LocalView.current

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -5 button
                        var minus5Pressed by remember { mutableStateOf(false) }
                        val minus5Scale by animateFloatAsState(
                            targetValue = if (minus5Pressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "minus5Scale"
                        )
                        LaunchedEffect(minus5Pressed) {
                            if (minus5Pressed) {
                                delay(100)
                                minus5Pressed = false
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                minus5Pressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.decrementAmount(5)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(minus5Scale)
                        ) {
                            Text("-5", fontWeight = FontWeight.Bold)
                        }

                        // -1 button
                        var minus1Pressed by remember { mutableStateOf(false) }
                        val minus1Scale by animateFloatAsState(
                            targetValue = if (minus1Pressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "minus1Scale"
                        )
                        LaunchedEffect(minus1Pressed) {
                            if (minus1Pressed) {
                                delay(100)
                                minus1Pressed = false
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                minus1Pressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.decrementAmount(1)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .scale(minus1Scale)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        // +1 button (main action) with bounce
                        var plus1Pressed by remember { mutableStateOf(false) }
                        val plus1Scale by animateFloatAsState(
                            targetValue = if (plus1Pressed) 1.15f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "plus1Scale"
                        )
                        LaunchedEffect(plus1Pressed) {
                            if (plus1Pressed) {
                                delay(100)
                                plus1Pressed = false
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                plus1Pressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.incrementAmount(1)
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .scale(plus1Scale),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // +5 button
                        var plus5Pressed by remember { mutableStateOf(false) }
                        val plus5Scale by animateFloatAsState(
                            targetValue = if (plus5Pressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "plus5Scale"
                        )
                        LaunchedEffect(plus5Pressed) {
                            if (plus5Pressed) {
                                delay(100)
                                plus5Pressed = false
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                plus5Pressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                viewModel.incrementAmount(5)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .scale(plus5Scale)
                        ) {
                            Text("+5", fontWeight = FontWeight.Bold)
                        }

                        // +10 button
                        var plus10Pressed by remember { mutableStateOf(false) }
                        val plus10Scale by animateFloatAsState(
                            targetValue = if (plus10Pressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "plus10Scale"
                        )
                        LaunchedEffect(plus10Pressed) {
                            if (plus10Pressed) {
                                delay(100)
                                plus10Pressed = false
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                plus10Pressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                viewModel.incrementAmount(10)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(plus10Scale)
                        ) {
                            Text("+10", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Complete button
                Button(
                    onClick = { viewModel.completeWorkout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.completedAmount > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.completedAmount >= uiState.targetAmount)
                            "Complete Workout"
                        else
                            "Finish Early",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
