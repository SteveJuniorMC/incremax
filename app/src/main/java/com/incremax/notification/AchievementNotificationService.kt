package com.incremax.notification

import com.incremax.domain.model.Achievement
import com.incremax.domain.repository.NotificationSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementNotificationService @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val notificationSettingsRepository: NotificationSettingsRepository
) {
    suspend fun onAchievementUnlocked(achievement: Achievement) {
        val settings = notificationSettingsRepository.getSettingsSync()
        if (settings.achievementNotificationsEnabled) {
            notificationHelper.showAchievementUnlocked(achievement.name, achievement.xpReward)
        }
    }

    suspend fun onLevelUp(newLevel: Int, levelTitle: String) {
        val settings = notificationSettingsRepository.getSettingsSync()
        if (settings.achievementNotificationsEnabled) {
            notificationHelper.showLevelUp(newLevel, levelTitle)
        }
    }
}
