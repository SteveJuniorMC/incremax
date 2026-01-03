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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.incremax.domain.model.Achievement
import com.incremax.domain.model.AchievementCategory
import com.incremax.domain.model.UserStats
import com.incremax.ui.theme.*
import kotlinx.coroutines.delay

sealed class CelebrationType {
    data class LevelUp(val previousLevel: Int, val newLevel: Int) : CelebrationType()
    data class AchievementUnlocked(val achievement: Achievement) : CelebrationType()
    data class StreakMilestone(val days: Int) : CelebrationType()
    data class WorkoutMilestone(val totalWorkouts: Int) : CelebrationType()
    data class PlanCompleted(val planName: String) : CelebrationType()
}

private val motivationalButtons = listOf(
    "I'm committed!",
    "I got this!",
    "I'm determined!",
    "Let's go!",
    "Unstoppable!",
    "Keep pushing!",
    "I'm on fire!",
    "Bring it on!",
    "Nothing stops me!",
    "I'm all in!"
)

@Composable
fun CelebrationScreen(
    celebrations: List<CelebrationType>,
    onDismiss: () -> Unit
) {
    if (celebrations.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    val currentCelebration = celebrations.getOrNull(currentIndex)

    if (currentCelebration == null) {
        onDismiss()
        return
    }

    val buttonText = remember { motivationalButtons.random() }

    CelebrationDialog(
        celebration = currentCelebration,
        buttonText = buttonText,
        onContinue = {
            if (currentIndex < celebrations.size - 1) {
                currentIndex++
            } else {
                onDismiss()
            }
        }
    )
}

@Composable
private fun CelebrationDialog(
    celebration: CelebrationType,
    buttonText: String,
    onContinue: () -> Unit
) {
    val view = LocalView.current

    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(celebration) {
        showContent = false
        showTitle = false
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        delay(100)
        showContent = true
        delay(400)
        showTitle = true
    }

    // Icon animation
    val iconScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Title animation
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(500),
        label = "titleAlpha"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (showTitle) 0f else 20f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "titleOffset"
    )

    val (icon, title, subtitle, description, primaryColor, gradientColors) = getCelebrationContent(celebration)

    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header text
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + scaleIn()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Main icon/badge with glow
                Box(
                    modifier = Modifier
                        .scale(iconScale)
                        .size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = glowAlpha))
                    )

                    // Main badge
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Subtitle (the main achievement text)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer(alpha = titleAlpha)
                        .offset(y = titleOffset.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer(alpha = titleAlpha)
                        .offset(y = titleOffset.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Motivational button
                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(tween(300)) + slideInVertically { it / 2 }
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(
                            buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class CelebrationContent(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val primaryColor: Color,
    val gradientColors: List<Color>
)

private fun getCelebrationContent(celebration: CelebrationType): CelebrationContent {
    return when (celebration) {
        is CelebrationType.LevelUp -> {
            val levelTitle = UserStats.getLevelTitle(celebration.newLevel)
            val (color, gradient) = getLevelColors(celebration.newLevel)
            CelebrationContent(
                icon = Icons.Default.KeyboardArrowUp,
                title = "LEVEL UP!",
                subtitle = "Level ${celebration.newLevel}",
                description = "You are now a $levelTitle",
                primaryColor = color,
                gradientColors = gradient
            )
        }
        is CelebrationType.AchievementUnlocked -> {
            val (color, gradient) = getAchievementColors(celebration.achievement.category)
            CelebrationContent(
                icon = getAchievementIcon(celebration.achievement.category),
                title = "ACHIEVEMENT UNLOCKED!",
                subtitle = celebration.achievement.name,
                description = celebration.achievement.description,
                primaryColor = color,
                gradientColors = gradient
            )
        }
        is CelebrationType.StreakMilestone -> {
            CelebrationContent(
                icon = Icons.Default.LocalFireDepartment,
                title = "STREAK MILESTONE!",
                subtitle = "${celebration.days} Day Streak!",
                description = "Your dedication is paying off. Keep the fire burning!",
                primaryColor = StreakFire,
                gradientColors = listOf(StreakFire, Color(0xFFFF8A65))
            )
        }
        is CelebrationType.WorkoutMilestone -> {
            CelebrationContent(
                icon = Icons.Default.FitnessCenter,
                title = "WORKOUT MILESTONE!",
                subtitle = "${celebration.totalWorkouts} Workouts!",
                description = "You've completed ${celebration.totalWorkouts} workouts. Incredible progress!",
                primaryColor = Primary,
                gradientColors = listOf(Primary, Color(0xFFFF8A65))
            )
        }
        is CelebrationType.PlanCompleted -> {
            CelebrationContent(
                icon = Icons.Default.EmojiEvents,
                title = "PLAN COMPLETED!",
                subtitle = celebration.planName,
                description = "You've crushed your goal. Time to set a new one!",
                primaryColor = Success,
                gradientColors = listOf(Success, Color(0xFF81C784))
            )
        }
    }
}

private fun getLevelColors(level: Int): Pair<Color, List<Color>> {
    return when {
        level <= 4 -> LevelBronze to listOf(LevelBronze, Color(0xFF8B4513))
        level <= 8 -> LevelSilver to listOf(LevelSilver, Color(0xFF808080))
        level <= 12 -> LevelGold to listOf(LevelGold, Color(0xFFDAA520))
        level <= 16 -> LevelPlatinum to listOf(LevelPlatinum, Color(0xFFB0B0B0))
        else -> LevelDiamond to listOf(LevelDiamond, Color(0xFF87CEEB))
    }
}

private fun getAchievementColors(category: AchievementCategory): Pair<Color, List<Color>> {
    return when (category) {
        AchievementCategory.STREAK -> StreakFire to listOf(StreakFire, Color(0xFFFF8A65))
        AchievementCategory.EXERCISE -> Primary to listOf(Primary, Color(0xFFFF9E80))
        AchievementCategory.LEVEL -> XpGold to listOf(XpGold, Color(0xFFFFE082))
        AchievementCategory.SPECIAL -> Color(0xFF7C4DFF) to listOf(Color(0xFF7C4DFF), Color(0xFFB388FF))
    }
}

private fun getAchievementIcon(category: AchievementCategory): ImageVector {
    return when (category) {
        AchievementCategory.STREAK -> Icons.Default.LocalFireDepartment
        AchievementCategory.EXERCISE -> Icons.Default.FitnessCenter
        AchievementCategory.LEVEL -> Icons.Default.Star
        AchievementCategory.SPECIAL -> Icons.Default.EmojiEvents
    }
}
