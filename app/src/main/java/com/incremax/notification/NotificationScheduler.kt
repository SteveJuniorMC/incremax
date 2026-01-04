package com.incremax.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.incremax.domain.model.NotificationSettings
import com.incremax.domain.model.WorkoutPlan
import com.incremax.domain.repository.WorkoutPlanRepository
import com.incremax.notification.worker.StreakAlertWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutPlanRepository: WorkoutPlanRepository
) {
    private val workManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedulePlanReminder(plan: WorkoutPlan) {
        val time = plan.reminderTime ?: return
        if (!plan.reminderEnabled) return

        val triggerTime = calculateTriggerTimeMillis(time)
        val requestCode = plan.id.hashCode()

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_PLAN_ID, plan.id)
            putExtra(ReminderAlarmReceiver.EXTRA_PLAN_NAME, plan.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarm for reliable timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm if permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelPlanReminder(planId: String) {
        val requestCode = planId.hashCode()
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    suspend fun scheduleAllPlanReminders() {
        // Schedule reminders for all plans that have them enabled
        val plans = workoutPlanRepository.getPlansWithRemindersSync()
        plans.forEach { plan ->
            schedulePlanReminder(plan)
        }
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

    fun scheduleWorkoutReminder(time: LocalTime) {
        // Schedule all per-plan reminders
        kotlinx.coroutines.runBlocking {
            scheduleAllPlanReminders()
        }
    }

    suspend fun cancelWorkoutReminder() {
        // Cancel all per-plan reminders
        val plans = workoutPlanRepository.getPlansWithRemindersSync()
        plans.forEach { plan ->
            cancelPlanReminder(plan.id)
        }
    }

    suspend fun updateSchedulesFromSettings(settings: NotificationSettings) {
        if (settings.workoutRemindersEnabled) {
            scheduleAllPlanReminders()
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

    private fun calculateTriggerTimeMillis(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var targetDateTime = now.toLocalDate().atTime(targetTime)

        if (now >= targetDateTime) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        return targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    companion object {
        const val STREAK_ALERT_WORK_NAME = "streak_alert"
        const val STREAK_ALERT_TAG = "streak_alert_tag"
    }
}
