package com.incremax.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.incremax.ui.theme.Primary
import com.incremax.ui.theme.XpGold
import com.incremax.ui.theme.Success
import com.incremax.ui.theme.Tertiary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shape: ConfettiShape
)

enum class ConfettiShape {
    RECTANGLE, CIRCLE, TRIANGLE
}

enum class CelebrationTier {
    BASIC,      // Simple workout completion
    PERFECT,    // Met or exceeded target
    MILESTONE   // New PR, streak milestone, level up
}

@Composable
fun ConfettiEffect(
    trigger: Boolean,
    tier: CelebrationTier = CelebrationTier.PERFECT,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val particleCount = when (tier) {
        CelebrationTier.BASIC -> 0 // No confetti for basic
        CelebrationTier.PERFECT -> 60
        CelebrationTier.MILESTONE -> 120
    }

    if (!trigger || particleCount == 0) return

    val colors = listOf(
        Primary,
        XpGold,
        Tertiary,
        Success,
        Color(0xFFFF8A65), // Light orange
        Color(0xFFFFD54F)  // Light gold
    )

    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            // Generate particles from center-top
            particles = List(particleCount) { index ->
                val angle = Random.nextFloat() * Math.PI.toFloat() // 0 to PI (upward spread)
                val speed = Random.nextFloat() * 800f + 400f // Velocity
                ConfettiParticle(
                    id = index,
                    startX = 0.5f, // Center
                    startY = 0.3f, // Upper third
                    velocityX = cos(angle) * speed * (if (Random.nextBoolean()) 1 else -1),
                    velocityY = -sin(angle) * speed * 0.5f, // Initial upward
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 720f - 360f,
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f,
                    shape = ConfettiShape.entries.random()
                )
            }

            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 2500,
                    easing = LinearEasing
                )
            )
            onComplete()
        }
    }

    val progress by animationProgress.asState()

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val gravity = 1200f // Gravity acceleration

        particles.forEach { particle ->
            val time = progress * 2.5f // Total time in seconds

            // Physics: position = initial + velocity*t + 0.5*g*t^2
            val x = width * particle.startX + particle.velocityX * time
            val y = height * particle.startY + particle.velocityY * time + 0.5f * gravity * time * time

            // Fade out in the last 30%
            val alpha = if (progress > 0.7f) {
                1f - ((progress - 0.7f) / 0.3f)
            } else 1f

            if (y < height + 50 && y > -50 && x > -50 && x < width + 50) {
                val currentRotation = particle.rotation + particle.rotationSpeed * time

                rotate(currentRotation, pivot = Offset(x, y)) {
                    when (particle.shape) {
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = particle.color.copy(alpha = alpha),
                                topLeft = Offset(x - particle.size / 2, y - particle.size / 4),
                                size = androidx.compose.ui.geometry.Size(particle.size, particle.size / 2)
                            )
                        }
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = particle.color.copy(alpha = alpha),
                                radius = particle.size / 2,
                                center = Offset(x, y)
                            )
                        }
                        ConfettiShape.TRIANGLE -> {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(x, y - particle.size / 2)
                                lineTo(x - particle.size / 2, y + particle.size / 2)
                                lineTo(x + particle.size / 2, y + particle.size / 2)
                                close()
                            }
                            drawPath(path, particle.color.copy(alpha = alpha))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarburstEffect(
    trigger: Boolean,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
            onComplete()
        }
    }

    val progress by animationProgress.asState()

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = maxOf(size.width, size.height)

        // Expanding ring
        val ringRadius = maxRadius * progress
        val ringAlpha = (1f - progress).coerceIn(0f, 0.5f)

        drawCircle(
            color = XpGold.copy(alpha = ringAlpha),
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
        )

        // Inner glow
        if (progress < 0.5f) {
            val glowAlpha = (0.5f - progress) * 0.6f
            drawCircle(
                color = XpGold.copy(alpha = glowAlpha),
                radius = ringRadius * 0.3f,
                center = Offset(centerX, centerY)
            )
        }

        // Rays
        val rayCount = 12
        val rayAlpha = (1f - progress).coerceIn(0f, 0.8f)
        repeat(rayCount) { i ->
            val angle = (i * 360f / rayCount) * (Math.PI / 180f).toFloat()
            val rayLength = maxRadius * progress * 0.8f
            val startOffset = 50f * progress

            drawLine(
                color = Primary.copy(alpha = rayAlpha),
                start = Offset(
                    centerX + cos(angle) * startOffset,
                    centerY + sin(angle) * startOffset
                ),
                end = Offset(
                    centerX + cos(angle) * rayLength,
                    centerY + sin(angle) * rayLength
                ),
                strokeWidth = 4f
            )
        }
    }
}
