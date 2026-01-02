package com.incremax.ui.screens.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.incremax.domain.model.WorkoutPlan
import com.incremax.ui.theme.Success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onPlanClick: (String) -> Unit,
    onCreatePlan: () -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showReminderPrompt && uiState.activatedPlan != null) {
        com.incremax.ui.components.ReminderPromptDialog(
            planName = uiState.activatedPlan!!.name,
            onSetReminder = { time -> viewModel.setReminder(time) },
            onSkip = { viewModel.skipReminder() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Plans", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onCreatePlan) {
                        Icon(Icons.Default.Add, contentDescription = "Create Plan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = "Create Plan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("My Plans") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Browse") }
                )
            }

            when (uiState.selectedTab) {
                0 -> MyPlansTab(
                    activePlans = uiState.activePlans,
                    completedPlans = uiState.completedPlans,
                    onPlanClick = onPlanClick,
                    onDeletePlan = viewModel::deletePlan
                )
                1 -> BrowsePlansTab(
                    presetPlans = uiState.presetPlans,
                    exercises = uiState.exercises,
                    onActivatePlan = viewModel::activatePresetPlan
                )
            }
        }
    }
}

@Composable
fun MyPlansTab(
    activePlans: List<PlanWithExercise>,
    completedPlans: List<PlanWithExercise>,
    onPlanClick: (String) -> Unit,
    onDeletePlan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (activePlans.isEmpty() && completedPlans.isEmpty()) {
            item {
                EmptyPlansMessage()
            }
        }

        if (activePlans.isNotEmpty()) {
            item {
                Text(
                    text = "Active Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(activePlans) { planWithExercise ->
                ActivePlanCard(
                    planWithExercise = planWithExercise,
                    onClick = { onPlanClick(planWithExercise.plan.id) },
                    onDelete = { onDeletePlan(planWithExercise.plan.id) }
                )
            }
        }

        if (completedPlans.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completed Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(completedPlans) { planWithExercise ->
                CompletedPlanCard(
                    planWithExercise = planWithExercise,
                    onClick = { onPlanClick(planWithExercise.plan.id) }
                )
            }
        }
    }
}

@Composable
fun ActivePlanCard(
    planWithExercise: PlanWithExercise,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plan") },
            text = { Text("Are you sure you want to delete '${planWithExercise.plan.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planWithExercise.plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = planWithExercise.exercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (planWithExercise.isCompletedToday) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Done today!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Success
                        )
                    } else {
                        Text(
                            text = "Today: ${planWithExercise.currentTarget} ${planWithExercise.exercise.unit}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "Goal: ${planWithExercise.plan.targetAmount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { planWithExercise.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(planWithExercise.progressPercentage * 100).toInt()}% complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CompletedPlanCard(
    planWithExercise: PlanWithExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planWithExercise.plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Reached ${planWithExercise.plan.targetAmount} ${planWithExercise.exercise.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Success,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BrowsePlansTab(
    presetPlans: List<WorkoutPlan>,
    exercises: List<com.incremax.domain.model.Exercise>,
    onActivatePlan: (WorkoutPlan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Start a Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(presetPlans) { plan ->
            val exercise = exercises.find { it.id == plan.exerciseId }
            PresetPlanCard(
                plan = plan,
                exerciseName = exercise?.name ?: plan.exerciseId,
                exerciseUnit = exercise?.unit ?: "reps",
                onActivate = { onActivatePlan(plan) }
            )
        }
    }
}

@Composable
fun PresetPlanCard(
    plan: WorkoutPlan,
    exerciseName: String,
    exerciseUnit: String,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start: ${plan.startingAmount} $exerciseUnit",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Goal: ${plan.targetAmount} $exerciseUnit",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "+${plan.incrementAmount} ${plan.incrementFrequency.name.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Button(onClick = onActivate) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
            }
        }
    }
}

@Composable
fun EmptyPlansMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No plans yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Browse preset plans or create your own",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
