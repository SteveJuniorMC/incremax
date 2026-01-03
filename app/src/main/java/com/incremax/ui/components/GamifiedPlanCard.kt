package com.incremax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.incremax.domain.model.IncrementFrequency
import com.incremax.domain.model.WorkoutPlan

/**
 * A gamified plan card that shows workout plans in an enticing way.
 * Used in both onboarding plan selection and the Browse tab.
 */
@Composable
fun GamifiedPlanCard(
    plan: WorkoutPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    actionButton: @Composable (() -> Unit)? = null
) {
    val accentColor = getAccentColorForExercise(plan.exerciseId)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top colored header with the BIG GOAL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // The aspirational goal
                    Text(
                        text = formatGoalHeadline(plan),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // "daily" / "every day" emphasis
                    Text(
                        text = "every single day",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Plan name and checkbox (if selectable)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelectable) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The "easy start" callout - this is the key selling point
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Start with just ${formatStartValueFriendly(plan)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Add ${formatIncrementFriendly(plan)} ${formatFrequencyText(plan.incrementFrequency)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration estimate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reach your goal in ${formatDurationFriendly(plan)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action button if provided
                actionButton?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    it()
                }
            }
        }
    }
}

// Helper functions

private fun getAccentColorForExercise(exerciseId: String): Color {
    return when (exerciseId) {
        "push_ups" -> Color(0xFF2196F3) // Blue
        "squats" -> Color(0xFF9C27B0) // Purple
        "sit_ups" -> Color(0xFFFF5722) // Deep Orange
        "plank" -> Color(0xFF009688) // Teal
        "running" -> Color(0xFF4CAF50) // Green
        "walking" -> Color(0xFF8BC34A) // Light Green
        "lunges" -> Color(0xFFE91E63) // Pink
        "burpees" -> Color(0xFFF44336) // Red
        "jumping_jacks" -> Color(0xFFFFEB3B) // Yellow
        "stretching" -> Color(0xFF00BCD4) // Cyan
        else -> Color(0xFF607D8B) // Blue Grey
    }
}

private fun formatGoalHeadline(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "push_ups" -> "${plan.targetAmount} push-ups"
        "squats" -> "${plan.targetAmount} squats"
        "sit_ups" -> "${plan.targetAmount} sit-ups"
        "plank" -> formatTimeFriendly(plan.targetAmount) + " plank"
        "running" -> formatDistanceFriendly(plan.targetAmount) + " run"
        "walking" -> formatDistanceFriendly(plan.targetAmount) + " walk"
        "lunges" -> "${plan.targetAmount} lunges"
        "burpees" -> "${plan.targetAmount} burpees"
        "jumping_jacks" -> "${plan.targetAmount} jumping jacks"
        "stretching" -> formatTimeFriendly(plan.targetAmount) + " stretch"
        else -> "${plan.targetAmount} reps"
    }
}

private fun formatTimeFriendly(seconds: Int): String {
    return when {
        seconds >= 60 && seconds % 60 == 0 -> "${seconds / 60}-minute"
        seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds}-second"
    }
}

private fun formatDistanceFriendly(meters: Int): String {
    return when {
        meters >= 1000 -> "${meters / 1000}K"
        else -> "${meters}m"
    }
}

private fun formatStartValueFriendly(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            val seconds = plan.startingAmount
            if (seconds >= 60) "${seconds / 60} minutes" else "$seconds seconds"
        }
        "running", "walking" -> {
            if (plan.startingAmount >= 1000) "${plan.startingAmount / 1000} km" else "${plan.startingAmount} meters"
        }
        else -> "${plan.startingAmount} ${getExerciseNameSimple(plan.exerciseId)}"
    }
}

private fun getExerciseNameSimple(exerciseId: String): String {
    return when (exerciseId) {
        "push_ups" -> "push-ups"
        "squats" -> "squats"
        "sit_ups" -> "sit-ups"
        "lunges" -> "lunges"
        "burpees" -> "burpees"
        "jumping_jacks" -> "jumping jacks"
        else -> "reps"
    }
}

private fun formatIncrementFriendly(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> "${plan.incrementAmount} more seconds"
        "running", "walking" -> "${plan.incrementAmount} more meters"
        else -> "${plan.incrementAmount} more"
    }
}

private fun formatFrequencyText(frequency: IncrementFrequency): String {
    return when (frequency) {
        IncrementFrequency.DAILY -> "each day"
        IncrementFrequency.WEEKLY -> "each week"
        IncrementFrequency.BIWEEKLY -> "every two weeks"
        IncrementFrequency.MONTHLY -> "each month"
    }
}

private fun formatDurationFriendly(plan: WorkoutPlan): String {
    val incrementsNeeded = (plan.targetAmount - plan.startingAmount) / plan.incrementAmount
    val weeks = when (plan.incrementFrequency) {
        IncrementFrequency.DAILY -> (incrementsNeeded + 6) / 7
        IncrementFrequency.WEEKLY -> incrementsNeeded
        IncrementFrequency.BIWEEKLY -> incrementsNeeded * 2
        IncrementFrequency.MONTHLY -> incrementsNeeded * 4
    }
    return when {
        weeks <= 1 -> "about 1 week"
        weeks < 8 -> "about $weeks weeks"
        weeks < 12 -> "about ${(weeks + 2) / 4} months"
        else -> "about ${(weeks + 2) / 4} months"
    }
}
