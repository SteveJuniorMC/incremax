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
        // Check if this is a plan-specific reminder
        val planId = inputData.getString(KEY_PLAN_ID)
        val planName = inputData.getString(KEY_PLAN_NAME)

        if (planId != null && planName != null) {
            // Per-plan reminder
            return handlePlanReminder(planId, planName)
        }

        // Legacy global reminder (for backwards compatibility)
        return handleGlobalReminder()
    }

    private suspend fun handlePlanReminder(planId: String, planName: String): Result {
        val today = LocalDate.now()

        // Check if the plan still exists and is active
        val plan = workoutPlanRepository.getPlanById(planId)
        if (plan == null || !plan.isActive || !plan.reminderEnabled) {
            return Result.success()
        }

        // Check if user already completed this plan today
        val todaySession = workoutSessionRepository.getSessionForPlanOnDate(planId, today)
        if (todaySession?.isCompleted == true) {
            return Result.success()
        }

        notificationHelper.showPlanReminder(planName)
        return Result.success()
    }

    private suspend fun handleGlobalReminder(): Result {
        val settings = notificationSettingsRepository.getSettingsSync()
        if (!settings.workoutRemindersEnabled) return Result.success()

        val today = LocalDate.now()
        val activePlans = workoutPlanRepository.getPlansWithRemindersSync()
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

    companion object {
        const val KEY_PLAN_ID = "plan_id"
        const val KEY_PLAN_NAME = "plan_name"
    }
}
