package com.incremax.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.incremax.data.repository.NotificationSettingsRepositoryImpl
import com.incremax.domain.repository.NotificationSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NotificationSettingsDataStore

private val Context.notificationSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_settings"
)

@Module
@InstallIn(SingletonComponent::class)
object NotificationDataStoreModule {
    @Provides
    @Singleton
    @NotificationSettingsDataStore
    fun provideNotificationSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.notificationSettingsDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        impl: NotificationSettingsRepositoryImpl
    ): NotificationSettingsRepository
}
