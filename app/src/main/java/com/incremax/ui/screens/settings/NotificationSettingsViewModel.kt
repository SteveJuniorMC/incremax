package com.incremax.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.NotificationSettings
import com.incremax.domain.repository.NotificationSettingsRepository
import com.incremax.notification.NotificationHelper
import com.incremax.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class NotificationSettingsUiState(
    val settings: NotificationSettings = NotificationSettings(),
    val hasPermission: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val notificationScheduler: NotificationScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            notificationSettingsRepository.getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        hasPermission = notificationHelper.hasNotificationPermission(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun hasNotificationPermission(): Boolean = notificationHelper.hasNotificationPermission()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) {
            notificationHelper.createNotificationChannels()
            viewModelScope.launch {
                val settings = notificationSettingsRepository.getSettingsSync()
                notificationScheduler.updateSchedulesFromSettings(settings)
            }
        }
    }

    fun toggleWorkoutReminders(enabled: Boolean) {
        viewModelScope.launch {
            notificationSettingsRepository.updateWorkoutRemindersEnabled(enabled)
            if (enabled) {
                val settings = notificationSettingsRepository.getSettingsSync()
                notificationScheduler.scheduleWorkoutReminder(settings.workoutReminderTime)
            } else {
                notificationScheduler.cancelWorkoutReminder()
            }
        }
    }

    fun updateWorkoutReminderTime(time: LocalTime) {
        viewModelScope.launch {
            notificationSettingsRepository.updateWorkoutReminderTime(time.hour, time.minute)
            notificationScheduler.scheduleWorkoutReminder(time)
        }
    }

    fun toggleStreakAlerts(enabled: Boolean) {
        viewModelScope.launch {
            notificationSettingsRepository.updateStreakAlertsEnabled(enabled)
            if (enabled) {
                val settings = notificationSettingsRepository.getSettingsSync()
                notificationScheduler.scheduleStreakAlert(settings.streakAlertTime)
            } else {
                notificationScheduler.cancelStreakAlert()
            }
        }
    }

    fun updateStreakAlertTime(time: LocalTime) {
        viewModelScope.launch {
            notificationSettingsRepository.updateStreakAlertTime(time.hour, time.minute)
            notificationScheduler.scheduleStreakAlert(time)
        }
    }

    fun toggleAchievementNotifications(enabled: Boolean) {
        viewModelScope.launch {
            notificationSettingsRepository.updateAchievementNotificationsEnabled(enabled)
        }
    }
}
