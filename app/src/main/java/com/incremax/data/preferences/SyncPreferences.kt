package com.incremax.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_preferences")

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val lastSyncTime: Flow<Long?> = context.syncDataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_TIME]
    }

    suspend fun setLastSyncTime(time: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = time
        }
    }

    suspend fun clear() {
        context.syncDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
