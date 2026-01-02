package com.incremax.domain.model

import java.time.LocalTime

data class NotificationSettings(
    val workoutRemindersEnabled: Boolean = true,
    val workoutReminderTime: LocalTime = LocalTime.of(9, 0),
    val streakAlertsEnabled: Boolean = true,
    val streakAlertTime: LocalTime = LocalTime.of(20, 0),
    val achievementNotificationsEnabled: Boolean = true
)
