package com.incremax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.incremax.domain.model.NotificationSettings
import com.incremax.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import com.incremax.di.NotificationSettingsDataStore
import javax.inject.Inject

class NotificationSettingsRepositoryImpl @Inject constructor(
    @NotificationSettingsDataStore private val dataStore: DataStore<Preferences>
) : NotificationSettingsRepository {

    companion object {
        val WORKOUT_REMINDERS_ENABLED = booleanPreferencesKey("workout_reminders_enabled")
        val WORKOUT_REMINDER_HOUR = intPreferencesKey("workout_reminder_hour")
        val WORKOUT_REMINDER_MINUTE = intPreferencesKey("workout_reminder_minute")
        val STREAK_ALERTS_ENABLED = booleanPreferencesKey("streak_alerts_enabled")
        val STREAK_ALERT_HOUR = intPreferencesKey("streak_alert_hour")
        val STREAK_ALERT_MINUTE = intPreferencesKey("streak_alert_minute")
        val ACHIEVEMENT_NOTIFICATIONS_ENABLED = booleanPreferencesKey("achievement_notifications_enabled")
    }

    override fun getSettings(): Flow<NotificationSettings> {
        return dataStore.data.map { prefs ->
            NotificationSettings(
                workoutRemindersEnabled = prefs[WORKOUT_REMINDERS_ENABLED] ?: true,
                workoutReminderTime = LocalTime.of(
                    prefs[WORKOUT_REMINDER_HOUR] ?: 9,
                    prefs[WORKOUT_REMINDER_MINUTE] ?: 0
                ),
                streakAlertsEnabled = prefs[STREAK_ALERTS_ENABLED] ?: true,
                streakAlertTime = LocalTime.of(
                    prefs[STREAK_ALERT_HOUR] ?: 20,
                    prefs[STREAK_ALERT_MINUTE] ?: 0
                ),
                achievementNotificationsEnabled = prefs[ACHIEVEMENT_NOTIFICATIONS_ENABLED] ?: true
            )
        }
    }

    override suspend fun getSettingsSync(): NotificationSettings = getSettings().first()

    override suspend fun updateWorkoutRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[WORKOUT_REMINDERS_ENABLED] = enabled }
    }

    override suspend fun updateWorkoutReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[WORKOUT_REMINDER_HOUR] = hour
            it[WORKOUT_REMINDER_MINUTE] = minute
        }
    }

    override suspend fun updateStreakAlertsEnabled(enabled: Boolean) {
        dataStore.edit { it[STREAK_ALERTS_ENABLED] = enabled }
    }

    override suspend fun updateStreakAlertTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[STREAK_ALERT_HOUR] = hour
            it[STREAK_ALERT_MINUTE] = minute
        }
    }

    override suspend fun updateAchievementNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[ACHIEVEMENT_NOTIFICATIONS_ENABLED] = enabled }
    }
}
