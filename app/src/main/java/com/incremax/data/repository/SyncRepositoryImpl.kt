package com.incremax.data.repository

import com.incremax.data.local.dao.*
import com.incremax.data.preferences.SyncPreferences
import com.incremax.data.remote.FirestoreDataSource
import com.incremax.domain.repository.SyncRepository
import com.incremax.domain.repository.SyncStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

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

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()

    override val lastSyncTime: Flow<Long?> = syncPreferences.lastSyncTime

    override suspend fun hasCloudData(userId: String): Boolean {
        return try {
            withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                firestoreDataSource.hasData(userId)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun hasLocalData(): Boolean {
        val stats = userStatsDao.getUserStatsSync()
        val plans = workoutPlanDao.getActivePlansSync()
        val sessions = workoutSessionDao.getAllSessionsSync()
        return stats != null || plans.isNotEmpty() || sessions.isNotEmpty()
    }

    override suspend fun performInitialSync(userId: String) {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val completed = withTimeoutOrNull(SYNC_TIMEOUT_MS * 2) {
                if (hasCloudData(userId)) {
                    syncFromCloud(userId)
                } else if (hasLocalData()) {
                    syncToCloud(userId)
                }
                true
            }
            _syncStatus.value = if (completed == true) SyncStatus.SUCCESS else SyncStatus.ERROR
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun replaceLocalWithCloud(userId: String) {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            // Clear local data
            workoutPlanDao.deleteAll()
            workoutSessionDao.deleteAll()
            userStatsDao.deleteStats()
            userStatsDao.deleteAchievements()
            userStatsDao.deleteExerciseTotals()

            // Download from cloud
            syncFromCloud(userId)

            syncPreferences.setLastSyncTime(System.currentTimeMillis())
            _syncStatus.value = SyncStatus.SUCCESS
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun uploadLocalToCloud(userId: String) {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            syncToCloud(userId)
            syncPreferences.setLastSyncTime(System.currentTimeMillis())
            _syncStatus.value = SyncStatus.SUCCESS
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    override suspend fun syncToCloud(userId: String) {
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

    override suspend fun syncFromCloud(userId: String) {
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
        try {
            syncToCloud(userId)
        } catch (e: Exception) {
            // Silently fail - will retry on next sync
        }
    }
}
