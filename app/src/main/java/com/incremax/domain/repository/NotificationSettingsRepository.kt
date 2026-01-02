package com.incremax.domain.repository

import com.incremax.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun getSettings(): Flow<NotificationSettings>
    suspend fun getSettingsSync(): NotificationSettings
    suspend fun updateWorkoutRemindersEnabled(enabled: Boolean)
    suspend fun updateWorkoutReminderTime(hour: Int, minute: Int)
    suspend fun updateStreakAlertsEnabled(enabled: Boolean)
    suspend fun updateStreakAlertTime(hour: Int, minute: Int)
    suspend fun updateAchievementNotificationsEnabled(enabled: Boolean)
}
