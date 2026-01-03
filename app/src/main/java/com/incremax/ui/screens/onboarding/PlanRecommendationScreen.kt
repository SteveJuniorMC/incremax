package com.incremax.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.WorkoutPlan
import com.incremax.ui.screens.onboarding.components.PlanSelectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanRecommendationScreen(
    recommendedPlans: List<WorkoutPlan>,
    selectedPlanIds: Set<String>,
    levelName: String,
    onTogglePlan: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val selectedCount = selectedPlanIds.size
    val selectedPlans = recommendedPlans.filter { it.id in selectedPlanIds }

    // Confirmation dialog
    if (showConfirmDialog) {
        PlanConfirmationDialog(
            selectedPlans = selectedPlans,
            onConfirm = {
                showConfirmDialog = false
                onContinue()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "$levelName Challenges",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Select which challenges you'd like to start",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Recommended Plans
            items(recommendedPlans) { plan ->
                PlanSelectionCard(
                    plan = plan,
                    isSelected = plan.id in selectedPlanIds,
                    onToggle = { onTogglePlan(plan.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedCount > 0
                ) {
                    Text(
                        text = if (selectedCount == 0) {
                            "Select at least one challenge"
                        } else if (selectedCount == 1) {
                            "Continue with 1 challenge"
                        } else {
                            "Continue with $selectedCount challenges"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanConfirmationDialog(
    selectedPlans: List<WorkoutPlan>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Ready to start?",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "You've selected ${selectedPlans.size} ${if (selectedPlans.size == 1) "challenge" else "challenges"}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                selectedPlans.forEach { plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Starting with ${formatStartingPoint(plan)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You can always add or remove challenges later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Let's go!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Go back")
            }
        }
    )
}

private fun formatStartingPoint(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            val seconds = plan.startingAmount
            if (seconds >= 60) "${seconds / 60} min" else "${seconds}s"
        }
        "running", "walking" -> {
            if (plan.startingAmount >= 1000) "${plan.startingAmount / 1000} km" else "${plan.startingAmount}m"
        }
        "push_ups" -> "${plan.startingAmount} push-ups"
        "squats" -> "${plan.startingAmount} squats"
        "sit_ups" -> "${plan.startingAmount} sit-ups"
        else -> "${plan.startingAmount} reps"
    }
}
