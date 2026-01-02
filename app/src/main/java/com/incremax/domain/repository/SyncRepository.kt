package com.incremax.domain.repository

import kotlinx.coroutines.flow.Flow

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

interface SyncRepository {
    val syncStatus: Flow<SyncStatus>
    val lastSyncTime: Flow<Long?>

    suspend fun hasCloudData(userId: String): Boolean
    suspend fun hasLocalData(): Boolean

    suspend fun performInitialSync(userId: String)
    suspend fun replaceLocalWithCloud(userId: String)
    suspend fun uploadLocalToCloud(userId: String)

    suspend fun syncToCloud(userId: String)
    suspend fun syncFromCloud(userId: String)

    suspend fun onLocalDataChanged(userId: String)
}
