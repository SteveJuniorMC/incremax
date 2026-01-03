package com.incremax.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.incremax.domain.model.Achievement
import com.incremax.domain.model.AchievementCategory
import com.incremax.domain.model.UserStats
import com.incremax.ui.theme.*
import kotlinx.coroutines.delay

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
fun RewardScreen(
    xpEarned: Int,
    totalXpBefore: Int,
    previousLevel: Int,
    newLevel: Int,
    currentStreak: Int,
    newAchievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val buttonText = remember { motivationalButtons.random() }
    val leveledUp = newLevel > previousLevel

    // Animation phases
    var showXpBar by remember { mutableStateOf(false) }
    var showLevelUp by remember { mutableStateOf(false) }
    var showStreak by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // XP bar animation
    val xpProgress by animateFloatAsState(
        targetValue = if (showXpBar) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "xpProgress"
    )

    LaunchedEffect(Unit) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        delay(300)
        showXpBar = true
        delay(1400)
        if (leveledUp) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            showLevelUp = true
            delay(800)
        }
        showStreak = true
        delay(400)
        if (newAchievements.isNotEmpty()) {
            showAchievements = true
            delay(400)
        }
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // XP Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // XP earned text
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = XpGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+$xpEarned XP",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = XpGold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // XP Progress Bar
                    XpProgressBar(
                        xpBefore = totalXpBefore,
                        xpEarned = xpEarned,
                        previousLevel = previousLevel,
                        newLevel = newLevel,
                        progress = xpProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Level Up Section
            AnimatedVisibility(
                visible = showLevelUp && leveledUp,
                enter = fadeIn() + scaleIn(initialScale = 0.8f)
            ) {
                LevelUpBadge(newLevel = newLevel)
            }

            if (leveledUp) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Streak Section
            AnimatedVisibility(
                visible = showStreak,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                StreakBadge(currentStreak = currentStreak)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Achievements Section
            AnimatedVisibility(
                visible = showAchievements && newAchievements.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Achievements Unlocked",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    newAchievements.forEach { achievement ->
                        AchievementBadge(achievement = achievement)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Motivational Button
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn() + slideInVertically { it }
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text(
                        buttonText,
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
private fun XpProgressBar(
    xpBefore: Int,
    xpEarned: Int,
    previousLevel: Int,
    newLevel: Int,
    progress: Float
) {
    val currentLevelXp = UserStats.xpForLevel(previousLevel)
    val nextLevelXp = UserStats.xpForNextLevel(previousLevel)
    val xpNeededForLevel = nextLevelXp - currentLevelXp

    // Calculate start and end progress within the level
    val startProgress = ((xpBefore - currentLevelXp).toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
    val xpAfter = xpBefore + xpEarned
    val endProgress = if (newLevel > previousLevel) {
        1f // Filled to 100% if leveled up
    } else {
        ((xpAfter - currentLevelXp).toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
    }

    val animatedProgress = startProgress + (endProgress - startProgress) * progress

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Level indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lvl $previousLevel",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Lvl ${previousLevel + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(XpGold, Primary)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // XP text
        val displayedXp = (xpBefore + (xpEarned * progress).toInt())
        Text(
            text = "$displayedXp / $nextLevelXp XP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LevelUpBadge(newLevel: Int) {
    val levelTitle = UserStats.getLevelTitle(newLevel)
    val levelColor = getLevelColor(newLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = levelColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(levelColor, levelColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$newLevel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "LEVEL UP!",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    letterSpacing = 2.sp
                )
                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(currentStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StreakFire.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = StreakFire,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "$currentStreak day streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (currentStreak == 1) "Great start!" else "Keep it going!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    val (color, _) = getAchievementColors(achievement.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getAchievementIcon(achievement.category),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "+${achievement.xpReward}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = XpGold
            )
        }
    }
}

private fun getLevelColor(level: Int): Color {
    return when {
        level <= 4 -> LevelBronze
        level <= 8 -> LevelSilver
        level <= 12 -> LevelGold
        level <= 16 -> LevelPlatinum
        else -> LevelDiamond
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
