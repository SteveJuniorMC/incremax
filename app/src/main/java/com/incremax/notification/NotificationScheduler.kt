package com.incremax.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.incremax.domain.model.NotificationSettings
import com.incremax.notification.worker.StreakAlertWorker
import com.incremax.notification.worker.WorkoutReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleWorkoutReminder(time: LocalTime) {
        val delay = calculateDelayUntil(time)

        val workRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORKOUT_REMINDER_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORKOUT_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelWorkoutReminder() {
        workManager.cancelUniqueWork(WORKOUT_REMINDER_WORK_NAME)
    }

    fun scheduleStreakAlert(time: LocalTime) {
        val delay = calculateDelayUntil(time)

        val workRequest = PeriodicWorkRequestBuilder<StreakAlertWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(STREAK_ALERT_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            STREAK_ALERT_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelStreakAlert() {
        workManager.cancelUniqueWork(STREAK_ALERT_WORK_NAME)
    }

    fun updateSchedulesFromSettings(settings: NotificationSettings) {
        if (settings.workoutRemindersEnabled) {
            scheduleWorkoutReminder(settings.workoutReminderTime)
        } else {
            cancelWorkoutReminder()
        }

        if (settings.streakAlertsEnabled) {
            scheduleStreakAlert(settings.streakAlertTime)
        } else {
            cancelStreakAlert()
        }
    }

    private fun calculateDelayUntil(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var targetDateTime = now.toLocalDate().atTime(targetTime)

        if (now >= targetDateTime) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        return Duration.between(now, targetDateTime).toMillis()
    }

    companion object {
        const val WORKOUT_REMINDER_WORK_NAME = "workout_reminder"
        const val WORKOUT_REMINDER_TAG = "workout_reminder_tag"
        const val STREAK_ALERT_WORK_NAME = "streak_alert"
        const val STREAK_ALERT_TAG = "streak_alert_tag"
    }
}
