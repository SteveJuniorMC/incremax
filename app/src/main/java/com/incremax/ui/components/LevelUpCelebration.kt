package com.incremax.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.incremax.domain.model.UserStats
import com.incremax.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LevelUpCelebration(
    previousLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    val newTitle = UserStats.getLevelTitle(newLevel)
    val levelColor = getLevelColor(newLevel)
    val levelGradient = getLevelGradient(newLevel)

    val view = LocalView.current

    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        delay(100)
        showContent = true
        delay(600)
        showTitle = true
    }

    // Level number animation
    val levelScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "levelScale"
    )

    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Level Up!" header
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + scaleIn()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = XpGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "LEVEL UP!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = XpGold,
                            letterSpacing = 4.sp
                        )
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = XpGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Level badge with glow
                Box(
                    modifier = Modifier
                        .scale(levelScale)
                        .size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(levelColor.copy(alpha = glowAlpha))
                    )

                    // Main badge
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(levelGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$newLevel",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 64.sp
                            )
                        }
                    }

                    // Star decorations
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = XpGold,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .graphicsLayer(alpha = glowAlpha + 0.3f)
                    )
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = XpGold,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopStart)
                            .graphicsLayer(alpha = glowAlpha + 0.3f)
                    )
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = XpGold,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 20.dp)
                            .graphicsLayer(alpha = glowAlpha + 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title reveal
                Text(
                    text = "You are now a",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .graphicsLayer(alpha = titleAlpha)
                        .offset(y = titleOffset.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = newTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .graphicsLayer(alpha = titleAlpha)
                        .offset(y = titleOffset.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Continue button
                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(tween(300)) + slideInVertically { it / 2 }
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = levelColor
                        )
                    ) {
                        Text(
                            "Continue",
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

private fun getLevelColor(level: Int): Color {
    return when {
        level <= 4 -> LevelBronze
        level <= 8 -> LevelSilver
        level <= 12 -> LevelGold
        level <= 16 -> LevelPlatinum
        else -> LevelDiamond
    }
}

private fun getLevelGradient(level: Int): List<Color> {
    return when {
        level <= 4 -> listOf(LevelBronze, Color(0xFF8B4513))
        level <= 8 -> listOf(LevelSilver, Color(0xFF808080))
        level <= 12 -> listOf(LevelGold, Color(0xFFDAA520))
        level <= 16 -> listOf(LevelPlatinum, Color(0xFFB0B0B0))
        else -> listOf(LevelDiamond, Color(0xFF87CEEB))
    }
}
