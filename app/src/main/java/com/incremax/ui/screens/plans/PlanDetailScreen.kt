package com.incremax.ui.screens.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.incremax.notification.NotificationScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class PlanDetailUiState(
    val plan: WorkoutPlan? = null,
    val exercise: Exercise? = null,
    val currentTarget: Int = 0,
    val progressPercentage: Float = 0f,
    val daysUntilGoal: Int = 0,
    val isLoading: Boolean = true,
    val showReminderTimePicker: Boolean = false
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val notificationScheduler: NotificationScheduler,
    private val notificationSettingsRepository: NotificationSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> = _uiState.asStateFlow()

    fun loadPlan(planId: String) {
        viewModelScope.launch {
            workoutPlanRepository.getPlanByIdFlow(planId).collect { plan ->
                if (plan != null) {
                    val exercise = exerciseRepository.getExerciseById(plan.exerciseId)
                    val today = LocalDate.now()
                    _uiState.update { current ->
                        PlanDetailUiState(
                            plan = plan,
                            exercise = exercise,
                            currentTarget = plan.getCurrentTarget(today),
                            progressPercentage = plan.getProgressPercentage(today),
                            daysUntilGoal = plan.getDaysUntilTarget(today),
                            isLoading = false,
                            showReminderTimePicker = current.showReminderTimePicker
                        )
                    }
                }
            }
        }
    }

    fun toggleReminder(enabled: Boolean) {
        val plan = _uiState.value.plan ?: return
        viewModelScope.launch {
            if (enabled) {
                // If enabling and no time set, show time picker
                if (plan.reminderTime == null) {
                    _uiState.update { it.copy(showReminderTimePicker = true) }
                } else {
                    notificationSettingsRepository.updateWorkoutRemindersEnabled(true)
                    workoutPlanRepository.updateReminder(plan.id, true, plan.reminderTime)
                    notificationScheduler.scheduleAllPlanReminders()
                }
            } else {
                workoutPlanRepository.updateReminder(plan.id, false, plan.reminderTime)
                notificationScheduler.cancelPlanReminder(plan.id)
            }
        }
    }

    fun setReminderTime(time: LocalTime) {
        val plan = _uiState.value.plan ?: return
        viewModelScope.launch {
            notificationSettingsRepository.updateWorkoutRemindersEnabled(true)
            workoutPlanRepository.updateReminder(plan.id, true, time)
            notificationScheduler.scheduleAllPlanReminders()
            _uiState.update { it.copy(showReminderTimePicker = false) }
        }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showReminderTimePicker = true) }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showReminderTimePicker = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    onStartWorkout: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }

    // Time Picker Dialog
    if (uiState.showReminderTimePicker) {
        val currentTime = uiState.plan?.reminderTime ?: LocalTime.of(9, 0)
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute
        )

        AlertDialog(
            onDismissRequest = { viewModel.hideTimePicker() },
            title = { Text("Set Reminder Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideTimePicker() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plan?.name ?: "Plan Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            uiState.plan?.let { plan ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Today's Target",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "${uiState.currentTarget}",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.exercise?.unit ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onStartWorkout,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start Workout")
                                }
                            }
                        }
                    }

                    // Progress Overview
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Progress Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress bar
                                LinearProgressIndicator(
                                    progress = { uiState.progressPercentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${plan.startingAmount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${(uiState.progressPercentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${plan.targetAmount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.daysUntilGoal > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "~${uiState.daysUntilGoal} days until goal",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Plan Details
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Plan Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow("Exercise", uiState.exercise?.name ?: "")
                                DetailRow("Starting", "${plan.startingAmount} ${uiState.exercise?.unit}")
                                DetailRow("Target", "${plan.targetAmount} ${uiState.exercise?.unit}")
                                DetailRow("Increment", "+${plan.incrementAmount} ${plan.incrementFrequency.name.lowercase()}")
                                DetailRow(
                                    "Started",
                                    plan.startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                )
                            }
                        }
                    }

                    // Reminder Settings
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Reminder",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (plan.reminderEnabled) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Daily Reminder",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (plan.reminderEnabled && plan.reminderTime != null) {
                                                Text(
                                                    text = plan.reminderTime.format(timeFormatter),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Switch(
                                        checked = plan.reminderEnabled,
                                        onCheckedChange = { viewModel.toggleReminder(it) }
                                    )
                                }

                                if (plan.reminderEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.showTimePicker() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Change Time")
                                    }
                                }
                            }
                        }
                    }

                    // Description
                    if (plan.description.isNotEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Description",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = plan.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
