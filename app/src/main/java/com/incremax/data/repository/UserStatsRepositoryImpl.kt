package com.incremax.data.repository

import com.incremax.data.local.dao.UserStatsDao
import com.incremax.data.local.dao.WorkoutPlanDao
import com.incremax.data.local.dao.WorkoutSessionDao
import com.incremax.data.local.entity.AchievementEntity
import com.incremax.data.local.entity.UserStatsEntity
import com.incremax.domain.model.*
import com.incremax.domain.repository.UserStatsRepository
import com.incremax.notification.AchievementNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class UserStatsRepositoryImpl @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val achievementNotificationService: AchievementNotificationService
) : UserStatsRepository {

    override fun getUserStats(): Flow<UserStats> {
        return userStatsDao.getUserStats().map { entity ->
            entity?.toDomain() ?: UserStats()
        }
    }

    override suspend fun getUserStatsSync(): UserStats {
        return userStatsDao.getUserStatsSync()?.toDomain() ?: UserStats()
    }

    override suspend fun initializeStats() {
        val existing = userStatsDao.getUserStatsSync()
        if (existing == null) {
            userStatsDao.insertOrUpdateStats(UserStatsEntity.fromDomain(UserStats()))
        }
        // Initialize achievements
        val achievementEntities = PresetAchievements.all.map {
            AchievementEntity(id = it.id, unlockedAt = null)
        }
        userStatsDao.insertAchievements(achievementEntities)
    }

    override suspend fun addXp(xp: Int) {
        val oldStats = userStatsDao.getUserStatsSync()
        val oldLevel = oldStats?.level ?: 1

        userStatsDao.addXp(xp)

        val stats = userStatsDao.getUserStatsSync() ?: return
        val newLevel = UserStats.calculateLevel(stats.totalXp)
        if (newLevel > stats.level) {
            userStatsDao.updateLevel(newLevel)
        }

        if (newLevel > oldLevel) {
            val levelTitle = UserStats.getLevelTitle(newLevel)
            achievementNotificationService.onLevelUp(newLevel, levelTitle)
        }
    }

    override suspend fun updateStreak(streak: Int) {
        userStatsDao.updateStreak(streak)
        val stats = userStatsDao.getUserStatsSync() ?: return
        if (streak > stats.longestStreak) {
            userStatsDao.updateLongestStreak(streak)
        }
    }

    override suspend fun updateLongestStreak(streak: Int) {
        userStatsDao.updateLongestStreak(streak)
    }

    override suspend fun incrementWorkouts() {
        userStatsDao.incrementWorkouts()
    }

    override suspend fun updateLastWorkoutDate(date: LocalDate) {
        userStatsDao.updateLastWorkoutDate(date)
    }

    override suspend fun addStreakFreezes(amount: Int) {
        userStatsDao.addStreakFreezes(amount)
    }

    override suspend fun useStreakFreeze(): Boolean {
        val stats = userStatsDao.getUserStatsSync() ?: return false
        if (stats.streakFreezes > 0) {
            userStatsDao.useStreakFreeze()
            return true
        }
        return false
    }

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return userStatsDao.getAllAchievements().map { entities ->
            PresetAchievements.all.map { preset ->
                val entity = entities.find { it.id == preset.id }
                preset.copy(unlockedAt = entity?.unlockedAt)
            }
        }
    }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return userStatsDao.getUnlockedAchievements().map { entities ->
            entities.mapNotNull { entity ->
                PresetAchievements.all.find { it.id == entity.id }?.copy(unlockedAt = entity.unlockedAt)
            }
        }
    }

    override suspend fun unlockAchievement(id: String) {
        val existing = userStatsDao.getAchievementById(id)
        if (existing?.unlockedAt == null) {
            userStatsDao.insertAchievement(
                AchievementEntity(id = id, unlockedAt = LocalDateTime.now())
            )
            // Award XP for achievement and send notification
            PresetAchievements.all.find { it.id == id }?.let { achievement ->
                achievementNotificationService.onAchievementUnlocked(achievement)
                addXp(achievement.xpReward)
            }
        }
    }

    override suspend fun getUnlockedAchievementCount(): Int {
        return userStatsDao.getUnlockedAchievementCount()
    }

    override suspend fun checkAndUnlockAchievements() {
        val stats = getUserStatsSync()
        val totalWorkouts = workoutSessionDao.getTotalSessionCount()
        val completedPlans = workoutPlanDao.getCompletedPlansCount()

        for (achievement in PresetAchievements.all) {
            val existing = userStatsDao.getAchievementById(achievement.id)
            if (existing?.unlockedAt != null) continue

            val shouldUnlock = when (val req = achievement.requirement) {
                is AchievementRequirement.StreakDays -> stats.currentStreak >= req.days || stats.longestStreak >= req.days
                is AchievementRequirement.TotalWorkouts -> totalWorkouts >= req.count
                is AchievementRequirement.ReachLevel -> stats.level >= req.level
                is AchievementRequirement.CompletePlans -> completedPlans >= req.count
                is AchievementRequirement.TotalXp -> stats.totalXp >= req.xp
                is AchievementRequirement.TotalExerciseCount -> {
                    val total = workoutSessionDao.getTotalForExercise(req.exerciseId) ?: 0L
                    total >= req.count
                }
            }

            if (shouldUnlock) {
                unlockAchievement(achievement.id)
            }
        }
    }

    override suspend fun getUnlockedAchievementIds(): Set<String> {
        return userStatsDao.getUnlockedAchievementIds().toSet()
    }

    override suspend fun getAchievementsByIds(ids: List<String>): List<Achievement> {
        return PresetAchievements.all.filter { it.id in ids }.map { preset ->
            val entity = userStatsDao.getAchievementById(preset.id)
            preset.copy(unlockedAt = entity?.unlockedAt)
        }
    }
}
