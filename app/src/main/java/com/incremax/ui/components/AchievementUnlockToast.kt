package com.incremax.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.Achievement
import com.incremax.domain.model.AchievementCategory
import com.incremax.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AchievementUnlockToast(
    achievement: Achievement,
    onDismiss: () -> Unit,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val view = LocalView.current

    LaunchedEffect(achievement) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        delay(50)
        isVisible = true
        delay(4000) // Auto-dismiss after 4 seconds
        isVisible = false
        delay(300)
        onDismiss()
    }

    // Slide animation
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -150f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "slideOffset"
    )

    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Shimmer animation for new achievement
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    val categoryGradient = getCategoryGradient(achievement.category)
    val categoryIcon = getCategoryIcon(achievement.category)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .offset(y = slideOffset.dp)
            .graphicsLayer(alpha = alpha)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clickable {
                    onTap()
                    onDismiss()
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                categoryGradient.first.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Achievement icon with gradient background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(categoryGradient.toList())),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Achievement details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryGradient.first,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                // XP reward badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = XpGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${achievement.xpReward}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockOverlay(
    achievements: List<Achievement>,
    onDismissAll: () -> Unit,
    onNavigateToAchievements: () -> Unit = {}
) {
    var currentIndex by remember { mutableStateOf(0) }

    if (achievements.isEmpty()) {
        onDismissAll()
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Show current achievement toast
        if (currentIndex < achievements.size) {
            AchievementUnlockToast(
                achievement = achievements[currentIndex],
                onDismiss = {
                    currentIndex++
                    if (currentIndex >= achievements.size) {
                        onDismissAll()
                    }
                },
                onTap = onNavigateToAchievements,
                modifier = Modifier.padding(top = 48.dp)
            )
        }
    }
}

private fun getCategoryGradient(category: AchievementCategory): Pair<Color, Color> {
    return when (category) {
        AchievementCategory.STREAK -> StreakFire to Color(0xFFFF8A65)
        AchievementCategory.EXERCISE -> Primary to Color(0xFFFF9E80)
        AchievementCategory.LEVEL -> XpGold to Color(0xFFFFE082)
        AchievementCategory.SPECIAL -> Color(0xFF7C4DFF) to Color(0xFFB388FF)
    }
}

private fun getCategoryIcon(category: AchievementCategory): ImageVector {
    return when (category) {
        AchievementCategory.STREAK -> Icons.Default.LocalFireDepartment
        AchievementCategory.EXERCISE -> Icons.Default.FitnessCenter
        AchievementCategory.LEVEL -> Icons.Default.Star
        AchievementCategory.SPECIAL -> Icons.Default.EmojiEvents
    }
}
