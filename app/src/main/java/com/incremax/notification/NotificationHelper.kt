package com.incremax.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.incremax.MainActivity
import com.incremax.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                NotificationChannels.WORKOUT_REMINDERS_ID,
                NotificationChannels.WORKOUT_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationChannels.WORKOUT_REMINDERS_DESC
            },
            NotificationChannel(
                NotificationChannels.STREAK_ALERTS_ID,
                NotificationChannels.STREAK_ALERTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationChannels.STREAK_ALERTS_DESC
            },
            NotificationChannel(
                NotificationChannels.ACHIEVEMENTS_ID,
                NotificationChannels.ACHIEVEMENTS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NotificationChannels.ACHIEVEMENTS_DESC
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showWorkoutReminder(incompleteCount: Int) {
        if (!hasNotificationPermission() || incompleteCount <= 0) return

        val (title, message) = if (incompleteCount == 1) {
            "One workout left!" to "You have 1 workout remaining today. Let's finish strong!"
        } else {
            "Time to work out!" to "You have $incompleteCount workouts scheduled for today. Let's get moving!"
        }

        showNotification(
            channelId = NotificationChannels.WORKOUT_REMINDERS_ID,
            notificationId = WORKOUT_REMINDER_NOTIFICATION_ID,
            title = title,
            message = message
        )
    }

    fun showStreakAlert(currentStreak: Int, hasWorkedOutToday: Boolean) {
        if (!hasNotificationPermission() || hasWorkedOutToday) return

        val (title, message) = when {
            currentStreak == 0 -> "Start your streak today!" to "Complete a workout to begin building your streak!"
            currentStreak >= 7 -> "Protect your $currentStreak-day streak!" to "You haven't worked out today. Don't let your amazing streak end!"
            else -> "Keep your streak alive!" to "You have a $currentStreak-day streak. Work out today to keep it going!"
        }

        showNotification(
            channelId = NotificationChannels.STREAK_ALERTS_ID,
            notificationId = STREAK_ALERT_NOTIFICATION_ID,
            title = title,
            message = message
        )
    }

    fun showAchievementUnlocked(achievementName: String, xpReward: Int) {
        if (!hasNotificationPermission()) return

        showNotification(
            channelId = NotificationChannels.ACHIEVEMENTS_ID,
            notificationId = ACHIEVEMENT_NOTIFICATION_ID_BASE + achievementName.hashCode(),
            title = "Achievement Unlocked!",
            message = "You earned \"$achievementName\" (+$xpReward XP)"
        )
    }

    fun showLevelUp(newLevel: Int, levelTitle: String) {
        if (!hasNotificationPermission()) return

        showNotification(
            channelId = NotificationChannels.ACHIEVEMENTS_ID,
            notificationId = LEVEL_UP_NOTIFICATION_ID,
            title = "Level Up!",
            message = "Congratulations! You reached Level $newLevel: $levelTitle"
        )
    }

    fun showPlanReminder(planName: String) {
        if (!hasNotificationPermission()) return

        showNotification(
            channelId = NotificationChannels.WORKOUT_REMINDERS_ID,
            notificationId = PLAN_REMINDER_NOTIFICATION_ID_BASE + planName.hashCode(),
            title = "Time for $planName!",
            message = "Your scheduled workout is waiting. Let's make progress today!"
        )
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val WORKOUT_REMINDER_NOTIFICATION_ID = 1001
        const val STREAK_ALERT_NOTIFICATION_ID = 1002
        const val ACHIEVEMENT_NOTIFICATION_ID_BASE = 2000
        const val LEVEL_UP_NOTIFICATION_ID = 3001
        const val PLAN_REMINDER_NOTIFICATION_ID_BASE = 4000
    }
}
