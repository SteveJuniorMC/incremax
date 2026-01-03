package com.incremax.data.repository

import android.util.Log
import com.incremax.data.local.dao.*
import com.incremax.data.preferences.SyncPreferences
import com.incremax.data.remote.FirestoreDataSource
import com.incremax.domain.repository.SyncRepository
import com.incremax.domain.repository.SyncStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SyncRepository"

private const val SYNC_TIMEOUT_MS = 15_000L

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val exerciseDao: ExerciseDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val userStatsDao: UserStatsDao,
    private val syncPreferences: SyncPreferences
) : SyncRepository {

    private val syncMutex = Mutex()
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()

    override val lastSyncTime: Flow<Long?> = syncPreferences.lastSyncTime

    override suspend fun hasCloudData(userId: String): Boolean {
        return try {
            withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                firestoreDataSource.hasData(userId)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check cloud data for user $userId", e)
            false
        }
    }

    override suspend fun hasLocalData(): Boolean {
        val stats = userStatsDao.getUserStatsSync()
        val plans = workoutPlanDao.getActivePlansSync()
        val sessions = workoutSessionDao.getAllSessionsSync()
        return stats != null || plans.isNotEmpty() || sessions.isNotEmpty()
    }

    override suspend fun performInitialSync(userId: String) = syncMutex.withLock {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val completed = withTimeoutOrNull(SYNC_TIMEOUT_MS * 2) {
                if (hasCloudData(userId)) {
                    syncFromCloudInternal(userId)
                } else if (hasLocalData()) {
                    syncToCloudInternal(userId)
                }
                true
            }
            _syncStatus.value = if (completed == true) SyncStatus.SUCCESS else SyncStatus.ERROR
        } catch (e: Exception) {
            Log.e(TAG, "Initial sync failed for user $userId", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun replaceLocalWithCloud(userId: String) = syncMutex.withLock {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            // Clear local data
            workoutPlanDao.deleteAll()
            workoutSessionDao.deleteAll()
            userStatsDao.deleteStats()
            userStatsDao.deleteAchievements()
            userStatsDao.deleteExerciseTotals()

            // Download from cloud
            syncFromCloudInternal(userId)

            syncPreferences.setLastSyncTime(System.currentTimeMillis())
            _syncStatus.value = SyncStatus.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "Failed to replace local data with cloud for user $userId", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun uploadLocalToCloud(userId: String) = syncMutex.withLock {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            syncToCloudInternal(userId)
            syncPreferences.setLastSyncTime(System.currentTimeMillis())
            _syncStatus.value = SyncStatus.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload local data to cloud for user $userId", e)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun syncToCloud(userId: String) = syncMutex.withLock {
        syncToCloudInternal(userId)
    }

    override suspend fun syncFromCloud(userId: String) = syncMutex.withLock {
        syncFromCloudInternal(userId)
    }

    private suspend fun syncToCloudInternal(userId: String) {
        // Upload all local data
        userStatsDao.getUserStatsSync()?.let {
            firestoreDataSource.uploadStats(userId, it)
        }

        firestoreDataSource.uploadExercises(userId, exerciseDao.getAllExercisesSync())
        firestoreDataSource.uploadPlans(userId, workoutPlanDao.getAllPlansSync())
        firestoreDataSource.uploadSessions(userId, workoutSessionDao.getAllSessionsSync())
        firestoreDataSource.uploadAchievements(userId, userStatsDao.getAllAchievementsSync())
        firestoreDataSource.uploadExerciseTotals(userId, exerciseDao.getAllTotalsSync())

        firestoreDataSource.updateLastSync(userId)
        syncPreferences.setLastSyncTime(System.currentTimeMillis())
    }

    private suspend fun syncFromCloudInternal(userId: String) {
        // Download and insert all cloud data
        firestoreDataSource.downloadStats(userId)?.let {
            userStatsDao.insertOrUpdateStats(it)
        }

        val exercises = firestoreDataSource.downloadExercises(userId)
        exercises.forEach { exerciseDao.insertExercise(it) }

        val plans = firestoreDataSource.downloadPlans(userId)
        plans.forEach { workoutPlanDao.insertPlan(it) }

        val sessions = firestoreDataSource.downloadSessions(userId)
        sessions.forEach { workoutSessionDao.insertSession(it) }

        val achievements = firestoreDataSource.downloadAchievements(userId)
        achievements.forEach { userStatsDao.insertAchievement(it) }

        val totals = firestoreDataSource.downloadExerciseTotals(userId)
        totals.forEach { exerciseDao.updateExerciseTotal(it) }

        syncPreferences.setLastSyncTime(System.currentTimeMillis())
    }

    override suspend fun onLocalDataChanged(userId: String) {
        // Use tryLock to avoid blocking - if sync is already in progress, skip this update
        if (syncMutex.tryLock()) {
            try {
                syncToCloudInternal(userId)
            } catch (e: Exception) {
                Log.w(TAG, "Background sync failed for user $userId - will retry on next sync", e)
            } finally {
                syncMutex.unlock()
            }
        } else {
            Log.d(TAG, "Skipping background sync - sync already in progress")
        }
    }
}
