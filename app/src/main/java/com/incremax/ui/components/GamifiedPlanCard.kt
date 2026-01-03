package com.incremax.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalFireDepartment
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
    val difficulty = calculateDifficulty(plan)
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
            // Top colored header with icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exercise icon
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getIconForExercise(plan.exerciseId),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Big goal display
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = formatGoalValue(plan),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                        Text(
                            text = formatGoalLabel(plan),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
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

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Duration
                    StatItem(
                        icon = Icons.Default.Schedule,
                        value = formatDuration(plan),
                        label = "Duration"
                    )

                    // Difficulty
                    DifficultyIndicator(difficulty = difficulty)

                    // Frequency
                    StatItem(
                        icon = Icons.Default.Refresh,
                        value = formatFrequencyShort(plan.incrementFrequency),
                        label = "Progress"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Journey description
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "START",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatStartValue(plan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Arrow
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )

                        // Increment
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatFrequencyShort(plan.incrementFrequency).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatIncrement(plan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }

                        // Arrow
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )

                        // Goal
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "GOAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatGoalShort(plan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
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

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DifficultyIndicator(difficulty: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (index < difficulty) {
                        when (difficulty) {
                            1, 2 -> Color(0xFF4CAF50) // Green - Easy
                            3 -> Color(0xFFFF9800) // Orange - Medium
                            else -> Color(0xFFF44336) // Red - Hard
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (difficulty) {
                1 -> "Easy"
                2 -> "Light"
                3 -> "Medium"
                4 -> "Hard"
                else -> "Intense"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions

private fun calculateDifficulty(plan: WorkoutPlan): Int {
    val totalIncrease = plan.targetAmount - plan.startingAmount
    val weeksToComplete = when (plan.incrementFrequency) {
        IncrementFrequency.DAILY -> (totalIncrease / plan.incrementAmount) / 7
        IncrementFrequency.WEEKLY -> totalIncrease / plan.incrementAmount
        IncrementFrequency.BIWEEKLY -> (totalIncrease / plan.incrementAmount) * 2
        IncrementFrequency.MONTHLY -> (totalIncrease / plan.incrementAmount) * 4
    }

    // Also consider the exercise type and target
    val intensityFactor = when (plan.exerciseId) {
        "burpees" -> 1.5f
        "running" -> 1.2f
        "push_ups" -> 1.1f
        "plank" -> 1.0f
        else -> 1.0f
    }

    val adjustedWeeks = weeksToComplete * intensityFactor

    return when {
        adjustedWeeks <= 8 -> 1
        adjustedWeeks <= 16 -> 2
        adjustedWeeks <= 24 -> 3
        adjustedWeeks <= 36 -> 4
        else -> 5
    }
}

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

private fun getIconForExercise(exerciseId: String): ImageVector {
    return when (exerciseId) {
        "push_ups" -> Icons.Default.FitnessCenter
        "squats" -> Icons.Default.DirectionsWalk
        "sit_ups" -> Icons.Default.SelfImprovement
        "plank" -> Icons.Default.AccessibilityNew
        "running" -> Icons.Default.DirectionsRun
        "walking" -> Icons.Default.DirectionsWalk
        "lunges" -> Icons.Default.DirectionsWalk
        "burpees" -> Icons.Default.SportsGymnastics
        "jumping_jacks" -> Icons.Default.Sports
        "stretching" -> Icons.Default.SelfImprovement
        else -> Icons.Default.FitnessCenter
    }
}

private fun formatGoalValue(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            val minutes = plan.targetAmount / 60
            if (minutes > 0) "$minutes" else "${plan.targetAmount}"
        }
        "running", "walking" -> {
            if (plan.targetAmount >= 1000) "${plan.targetAmount / 1000}" else "${plan.targetAmount}"
        }
        else -> "${plan.targetAmount}"
    }
}

private fun formatGoalLabel(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "push_ups" -> "push-ups"
        "squats" -> "squats"
        "sit_ups" -> "sit-ups"
        "plank", "stretching" -> if (plan.targetAmount >= 60) "minutes" else "seconds"
        "running" -> if (plan.targetAmount >= 1000) "kilometers" else "meters"
        "walking" -> if (plan.targetAmount >= 1000) "kilometers" else "meters"
        "lunges" -> "lunges"
        "burpees" -> "burpees"
        "jumping_jacks" -> "jumping jacks"
        else -> "reps"
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
        weeks <= 1 -> "1 wk"
        weeks < 8 -> "$weeks wks"
        else -> "${(weeks + 3) / 4} mo"
    }
}

private fun formatFrequencyShort(frequency: IncrementFrequency): String {
    return when (frequency) {
        IncrementFrequency.DAILY -> "Daily"
        IncrementFrequency.WEEKLY -> "Weekly"
        IncrementFrequency.BIWEEKLY -> "Bi-weekly"
        IncrementFrequency.MONTHLY -> "Monthly"
    }
}

private fun formatStartValue(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            val seconds = plan.startingAmount
            if (seconds >= 60) "${seconds / 60}m" else "${seconds}s"
        }
        "running", "walking" -> {
            if (plan.startingAmount >= 1000) "${plan.startingAmount / 1000}K" else "${plan.startingAmount}m"
        }
        else -> "${plan.startingAmount}"
    }
}

private fun formatIncrement(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> "+${plan.incrementAmount}s"
        "running", "walking" -> "+${plan.incrementAmount}m"
        else -> "+${plan.incrementAmount}"
    }
}

private fun formatGoalShort(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            val seconds = plan.targetAmount
            if (seconds >= 60) "${seconds / 60}m" else "${seconds}s"
        }
        "running", "walking" -> {
            if (plan.targetAmount >= 1000) "${plan.targetAmount / 1000}K" else "${plan.targetAmount}m"
        }
        else -> "${plan.targetAmount}"
    }
}
