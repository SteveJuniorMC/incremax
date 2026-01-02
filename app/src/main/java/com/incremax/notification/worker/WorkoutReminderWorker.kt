package com.incremax.notification.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.incremax.domain.repository.NotificationSettingsRepository
import com.incremax.domain.repository.WorkoutPlanRepository
import com.incremax.domain.repository.WorkoutSessionRepository
import com.incremax.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class WorkoutReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutSessionRepository: WorkoutSessionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settings = notificationSettingsRepository.getSettingsSync()
        if (!settings.workoutRemindersEnabled) return Result.success()

        val today = LocalDate.now()
        val activePlans = workoutPlanRepository.getActivePlans().first()
        val todaySessions = workoutSessionRepository.getSessionsByDateSync(today)

        val completedPlanIds = todaySessions
            .filter { it.isCompleted }
            .map { it.planId }
            .toSet()

        val incompleteCount = activePlans.count { it.id !in completedPlanIds }

        if (incompleteCount > 0) {
            notificationHelper.showWorkoutReminder(incompleteCount)
        }

        return Result.success()
    }
}
