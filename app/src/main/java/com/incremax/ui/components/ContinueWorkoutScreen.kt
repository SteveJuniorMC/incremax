package com.incremax.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.DifficultyLevel
import com.incremax.domain.model.WorkoutPlan
import kotlinx.coroutines.delay

@Composable
fun ContinueWorkoutScreen(
    remainingPlans: List<WorkoutPlan>,
    onSelectPlan: (WorkoutPlan) -> Unit,
    onFinish: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showPlans by remember { mutableStateOf(false) }
    var showFinishButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
        delay(300)
        showPlans = true
        delay(300)
        showFinishButton = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Keep going?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You have ${remainingPlans.size} more ${if (remainingPlans.size == 1) "challenge" else "challenges"} for today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Plan buttons
            AnimatedVisibility(
                visible = showPlans,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    remainingPlans.forEach { plan ->
                        PlanButton(
                            plan = plan,
                            onClick = { onSelectPlan(plan) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Finish button
            AnimatedVisibility(
                visible = showFinishButton,
                enter = fadeIn() + slideInVertically { it }
            ) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Done for today",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlanButton(
    plan: WorkoutPlan,
    onClick: () -> Unit
) {
    val difficultyColor = when (plan.difficulty) {
        DifficultyLevel.BEGINNER -> Color(0xFF4CAF50)
        DifficultyLevel.INTERMEDIATE -> Color(0xFFFF9800)
        DifficultyLevel.ADVANCED -> Color(0xFFF44336)
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = difficultyColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTarget(plan),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Start",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatTarget(plan: WorkoutPlan): String {
    val target = plan.getCurrentTarget(java.time.LocalDate.now())
    return when (plan.exerciseId) {
        "plank", "stretching" -> {
            if (target >= 60) "${target / 60} min" else "${target}s"
        }
        "running", "walking" -> {
            if (target >= 1000) "${target / 1000} km" else "${target}m"
        }
        "push_ups" -> "$target push-ups"
        "squats" -> "$target squats"
        "sit_ups" -> "$target sit-ups"
        else -> "$target reps"
    }
}
