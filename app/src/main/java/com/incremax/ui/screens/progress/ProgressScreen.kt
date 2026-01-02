package com.incremax.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
import com.incremax.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class ProgressUiState(
    val userStats: UserStats = UserStats(),
    val totalWorkouts: Int = 0,
    val uniqueDays: Int = 0,
    val recentSessions: List<WorkoutSession> = emptyList(),
    val workoutDates: List<LocalDate> = emptyList(),
    val exerciseTotals: Map<String, Long> = emptyMap(),
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userStatsRepository.getUserStats(),
                workoutSessionRepository.getAllSessions(),
                workoutSessionRepository.getWorkoutDates(),
                exerciseRepository.getAllExerciseTotals(),
                exerciseRepository.getAllExercises()
            ) { stats, sessions, dates, totals, exercises ->
                ProgressUiState(
                    userStats = stats,
                    totalWorkouts = sessions.size,
                    uniqueDays = dates.size,
                    recentSessions = sessions.take(10),
                    workoutDates = dates,
                    exerciseTotals = totals,
                    exercises = exercises,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Stats Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Workouts",
                        value = "${uiState.totalWorkouts}",
                        icon = Icons.Default.FitnessCenter
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Days",
                        value = "${uiState.uniqueDays}",
                        icon = Icons.Default.CalendarMonth
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Current Streak",
                        value = "${uiState.userStats.currentStreak}",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = StreakFire
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total XP",
                        value = "${uiState.userStats.totalXp}",
                        icon = Icons.Default.Star,
                        iconTint = XpGold
                    )
                }
            }

            // Activity Calendar (simplified heatmap)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Activity (Last 5 Weeks)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ActivityHeatmap(workoutDates = uiState.workoutDates)
                    }
                }
            }

            // Exercise Totals
            if (uiState.exerciseTotals.isNotEmpty()) {
                item {
                    Text(
                        text = "Lifetime Totals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.exerciseTotals.entries.toList()) { (exerciseId, total) ->
                            val exercise = uiState.exercises.find { it.id == exerciseId }
                            if (exercise != null) {
                                ExerciseTotalCard(
                                    exercise = exercise,
                                    total = total
                                )
                            }
                        }
                    }
                }
            }

            // Recent Activity
            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(uiState.recentSessions.take(5)) { session ->
                    val exercise = uiState.exercises.find { it.id == session.exerciseId }
                    RecentSessionCard(session = session, exercise = exercise)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ActivityHeatmap(workoutDates: List<LocalDate>) {
    val today = LocalDate.now()
    val startDate = today.minusDays(34)
    val dateSet = workoutDates.toSet()

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Days of week labels
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (week in 0 until 5) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (day in 0 until 7) {
                        val date = startDate.plusDays((week * 7 + day).toLong())
                        val hasWorkout = date in dateSet
                        val isToday = date == today
                        val isFuture = date > today

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        hasWorkout -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTotalCard(
    exercise: Exercise,
    total: Long
) {
    Card(
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTotal(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = exercise.unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RecentSessionCard(
    session: WorkoutSession,
    exercise: Exercise?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = exercise?.name ?: "Exercise",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = session.date.format(DateTimeFormatter.ofPattern("MMM d")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${session.completedAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " ${exercise?.unit ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "+${session.xpEarned} XP",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

private fun formatTotal(total: Long): String {
    return when {
        total >= 1_000_000 -> "%.1fM".format(total / 1_000_000.0)
        total >= 1_000 -> "%.1fK".format(total / 1_000.0)
        else -> total.toString()
    }
}
