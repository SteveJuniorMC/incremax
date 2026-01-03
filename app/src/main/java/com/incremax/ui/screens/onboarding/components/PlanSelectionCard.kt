package com.incremax.ui.screens.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.IncrementFrequency
import com.incremax.domain.model.WorkoutPlan

@Composable
fun PlanSelectionCard(
    plan: WorkoutPlan,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Goal and Duration chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Goal chip
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.FlagCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = formatGoal(plan),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Duration chip
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = formatDuration(plan),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress hint
                Text(
                    text = formatProgressHint(plan),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatGoal(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "push_ups" -> "${plan.targetAmount} push-ups"
        "squats" -> "${plan.targetAmount} squats"
        "sit_ups" -> "${plan.targetAmount} sit-ups"
        "plank" -> formatTime(plan.targetAmount)
        "running" -> formatDistance(plan.targetAmount)
        "walking" -> formatDistance(plan.targetAmount)
        "lunges" -> "${plan.targetAmount} lunges"
        "burpees" -> "${plan.targetAmount} burpees"
        "jumping_jacks" -> "${plan.targetAmount} jumping jacks"
        "stretching" -> formatTime(plan.targetAmount)
        else -> "${plan.targetAmount}"
    }
}

private fun formatTime(seconds: Int): String {
    return when {
        seconds >= 60 && seconds % 60 == 0 -> "${seconds / 60} min"
        seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

private fun formatDistance(meters: Int): String {
    return when {
        meters >= 1000 -> "${meters / 1000}K"
        else -> "${meters}m"
    }
}

private fun formatDuration(plan: WorkoutPlan): String {
    val incrementsNeeded = (plan.targetAmount - plan.startingAmount) / plan.incrementAmount
    val weeks = when (plan.incrementFrequency) {
        IncrementFrequency.DAILY -> (incrementsNeeded + 6) / 7
        IncrementFrequency.WEEKLY -> incrementsNeeded
        IncrementFrequency.BIWEEKLY -> incrementsNeeded * 2
        IncrementFrequency.MONTHLY -> incrementsNeeded * 4
    }
    return when {
        weeks <= 1 -> "~1 week"
        weeks < 4 -> "~$weeks weeks"
        weeks < 8 -> "~${(weeks + 1) / 2} weeks"
        else -> "~${(weeks + 3) / 4} months"
    }
}

private fun formatProgressHint(plan: WorkoutPlan): String {
    val frequencyText = when (plan.incrementFrequency) {
        IncrementFrequency.DAILY -> "daily"
        IncrementFrequency.WEEKLY -> "weekly"
        IncrementFrequency.BIWEEKLY -> "every 2 weeks"
        IncrementFrequency.MONTHLY -> "monthly"
    }

    val incrementText = when (plan.exerciseId) {
        "plank", "stretching" -> "+${plan.incrementAmount}s"
        "running", "walking" -> "+${plan.incrementAmount}m"
        else -> "+${plan.incrementAmount}"
    }

    return "Start at ${formatStartValue(plan)} · $incrementText $frequencyText"
}

private fun formatStartValue(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> formatTime(plan.startingAmount)
        "running", "walking" -> formatDistance(plan.startingAmount)
        else -> "${plan.startingAmount}"
    }
}
