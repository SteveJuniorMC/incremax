package com.incremax.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.incremax.domain.repository.WorkoutPlanRepository
import com.incremax.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var workoutPlanRepository: WorkoutPlanRepository
    @Inject lateinit var workoutSessionRepository: WorkoutSessionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getStringExtra(EXTRA_PLAN_ID)
        val planName = intent.getStringExtra(EXTRA_PLAN_NAME)

        if (planId != null && planName != null) {
            handlePlanReminder(planId, planName)
        }
    }

    private fun handlePlanReminder(planId: String, planName: String) {
        scope.launch {
            val today = LocalDate.now()

            // Check if the plan still exists and is active
            val plan = workoutPlanRepository.getPlanById(planId)
            if (plan == null || !plan.isActive || !plan.reminderEnabled) {
                return@launch
            }

            // Check if user already completed this plan today
            val todaySession = workoutSessionRepository.getSessionForPlanOnDate(planId, today)
            if (todaySession?.isCompleted == true) {
                return@launch
            }

            // Show the notification
            notificationHelper.showPlanReminder(planName)

            // Reschedule for tomorrow
            notificationScheduler.schedulePlanReminder(plan)
        }
    }

    companion object {
        const val EXTRA_PLAN_ID = "plan_id"
        const val EXTRA_PLAN_NAME = "plan_name"
    }
}
