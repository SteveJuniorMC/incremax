package com.incremax.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.incremax.domain.repository.NotificationSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationSettingsRepository: NotificationSettingsRepository
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scope.launch {
                notificationHelper.createNotificationChannels()
                val settings = notificationSettingsRepository.getSettingsSync()
                notificationScheduler.updateSchedulesFromSettings(settings)
            }
        }
    }
}
