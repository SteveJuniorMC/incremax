package com.incremax.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.incremax.domain.model.DifficultyLevel
import com.incremax.domain.model.WorkoutPlan
import kotlinx.coroutines.delay

@Composable
fun ChallengeCompleteScreen(
    completedPlan: WorkoutPlan,
    onContinue: () -> Unit
) {
    val view = LocalView.current
    val difficultyColor = getDifficultyColor(completedPlan.difficulty)

    // Animation states
    var showTrophy by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showPlanName by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trophy bounce animation
    val trophyScale by animateFloatAsState(
        targetValue = if (showTrophy) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "trophyScale"
    )

    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        // Trigger haptic feedback
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        delay(200)
        showTrophy = true
        delay(500)
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        showTitle = true
        delay(300)
        showPlanName = true
        delay(300)
        showStats = true
        delay(400)
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            // Trophy with glow
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                if (showTrophy) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(trophyScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        difficultyColor.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                // Trophy circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(trophyScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    difficultyColor,
                                    difficultyColor.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Text(
                    text = "CHALLENGE COMPLETE!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plan name
            AnimatedVisibility(
                visible = showPlanName,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Text(
                    text = completedPlan.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Achievement stats
            AnimatedVisibility(
                visible = showStats,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You achieved",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatAchievement(completedPlan),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = difficultyColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "every single day",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(
                                label = "Started at",
                                value = formatAmount(completedPlan.startingAmount, completedPlan.exerciseId)
                            )
                            StatColumn(
                                label = "Reached",
                                value = formatAmount(completedPlan.targetAmount, completedPlan.exerciseId),
                                highlight = true,
                                highlightColor = difficultyColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn() + slideInVertically { it }
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = difficultyColor
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    highlight: Boolean = false,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) highlightColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getDifficultyColor(difficulty: DifficultyLevel): Color {
    return when (difficulty) {
        DifficultyLevel.BEGINNER -> Color(0xFF4CAF50)
        DifficultyLevel.INTERMEDIATE -> Color(0xFFFF9800)
        DifficultyLevel.ADVANCED -> Color(0xFFF44336)
    }
}

private fun formatAchievement(plan: WorkoutPlan): String {
    return when (plan.exerciseId) {
        "push_ups" -> "${plan.targetAmount} push-ups"
        "squats" -> "${plan.targetAmount} squats"
        "sit_ups" -> "${plan.targetAmount} sit-ups"
        "plank" -> formatTime(plan.targetAmount)
        "running" -> formatDistance(plan.targetAmount)
        else -> "${plan.targetAmount}"
    }
}

private fun formatAmount(amount: Int, exerciseId: String): String {
    return when (exerciseId) {
        "plank", "stretching" -> formatTime(amount)
        "running", "walking" -> formatDistance(amount)
        else -> "$amount"
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
