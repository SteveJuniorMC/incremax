package com.incremax.ui.screens.workout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
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
    val showCompletionDialog: Boolean = false
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

            // Fixed XP per workout - no bonuses to discourage fudging data
            val userStats = userStatsRepository.getUserStatsSync()
            val xpEarned = XpRewards.calculateWorkoutXp()

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
            val lastWorkoutDate = userStats.lastWorkoutDate
            val newStreak = when {
                lastWorkoutDate == null -> 1
                lastWorkoutDate == today -> userStats.currentStreak
                lastWorkoutDate == today.minusDays(1) -> userStats.currentStreak + 1
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

            _uiState.update {
                it.copy(
                    isCompleted = true,
                    xpEarned = xpEarned,
                    showCompletionDialog = true
                )
            }
        }
    }

    fun dismissCompletionDialog() {
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

    // Completion Dialog
    if (uiState.showCompletionDialog) {
        WorkoutCompletionDialog(
            xpEarned = uiState.xpEarned,
            completedAmount = uiState.completedAmount,
            targetAmount = uiState.targetAmount,
            exerciseUnit = uiState.exercise?.unit ?: "reps",
            onDismiss = { viewModel.dismissCompletionDialog() }
        )
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

                    // Counter controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -5 button
                        FilledTonalIconButton(
                            onClick = { viewModel.decrementAmount(5) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("-5", fontWeight = FontWeight.Bold)
                        }

                        // -1 button
                        FilledTonalIconButton(
                            onClick = { viewModel.decrementAmount(1) },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        // +1 button (main action)
                        val scale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "scale"
                        )

                        FloatingActionButton(
                            onClick = { viewModel.incrementAmount(1) },
                            modifier = Modifier
                                .size(80.dp)
                                .scale(scale),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // +5 button
                        FilledTonalIconButton(
                            onClick = { viewModel.incrementAmount(5) },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Text("+5", fontWeight = FontWeight.Bold)
                        }

                        // +10 button
                        FilledTonalIconButton(
                            onClick = { viewModel.incrementAmount(10) },
                            modifier = Modifier.size(48.dp)
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

@Composable
fun WorkoutCompletionDialog(
    xpEarned: Int,
    completedAmount: Int,
    targetAmount: Int,
    exerciseUnit: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Workout Complete!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$completedAmount / $targetAmount $exerciseUnit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+$xpEarned XP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    )
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
