package com.incremax.notification.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.incremax.domain.repository.NotificationSettingsRepository
import com.incremax.domain.repository.UserStatsRepository
import com.incremax.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class StreakAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userStatsRepository: UserStatsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settings = notificationSettingsRepository.getSettingsSync()
        if (!settings.streakAlertsEnabled) return Result.success()

        val stats = userStatsRepository.getUserStatsSync()
        val today = LocalDate.now()
        val hasWorkedOutToday = stats.lastWorkoutDate == today

        notificationHelper.showStreakAlert(stats.currentStreak, hasWorkedOutToday)

        return Result.success()
    }
}
