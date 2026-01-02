package com.incremax.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.UserStatsRepository
import com.incremax.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AchievementsUiState(
    val unlockedAchievements: List<Achievement> = emptyList(),
    val lockedAchievements: List<Achievement> = emptyList(),
    val totalUnlocked: Int = 0,
    val totalAchievements: Int = PresetAchievements.all.size,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            userStatsRepository.getAllAchievements().collect { achievements ->
                val unlocked = achievements.filter { it.isUnlocked }
                    .sortedByDescending { it.unlockedAt }
                val locked = achievements.filter { !it.isUnlocked }

                _uiState.value = AchievementsUiState(
                    unlockedAchievements = unlocked,
                    lockedAchievements = locked,
                    totalUnlocked = unlocked.size,
                    isLoading = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Progress Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = XpGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${uiState.totalUnlocked} / ${uiState.totalAchievements}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "achievements unlocked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { uiState.totalUnlocked.toFloat() / uiState.totalAchievements },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                            color = XpGold,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Unlocked Achievements
            if (uiState.unlockedAchievements.isNotEmpty()) {
                item {
                    Text(
                        text = "Unlocked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.unlockedAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isUnlocked = true
                    )
                }
            }

            // Locked Achievements
            if (uiState.lockedAchievements.isNotEmpty()) {
                item {
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.lockedAchievements.filter { !it.isHidden }) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isUnlocked = false
                    )
                }

                // Hidden achievements placeholder
                val hiddenCount = uiState.lockedAchievements.count { it.isHidden }
                if (hiddenCount > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$hiddenCount hidden achievements",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean
) {
    val icon = getAchievementIcon(achievement.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.7f),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) {
                            Brush.linearGradient(
                                colors = listOf(XpGold, StreakFire)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (isUnlocked && achievement.unlockedAt != null) {
                    Text(
                        text = "Unlocked ${achievement.unlockedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // XP Reward
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = "+${achievement.xpReward}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

private fun getAchievementIcon(category: AchievementCategory): ImageVector {
    return when (category) {
        AchievementCategory.STREAK -> Icons.Default.LocalFireDepartment
        AchievementCategory.EXERCISE -> Icons.Default.FitnessCenter
        AchievementCategory.LEVEL -> Icons.Default.TrendingUp
        AchievementCategory.SPECIAL -> Icons.Default.EmojiEvents
    }
}
