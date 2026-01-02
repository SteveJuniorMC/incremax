package com.incremax.domain.repository

import com.incremax.domain.model.Achievement
import com.incremax.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UserStatsRepository {
    fun getUserStats(): Flow<UserStats>
    suspend fun getUserStatsSync(): UserStats
    suspend fun initializeStats()
    suspend fun addXp(xp: Int)
    suspend fun updateStreak(streak: Int)
    suspend fun updateLongestStreak(streak: Int)
    suspend fun incrementWorkouts()
    suspend fun updateLastWorkoutDate(date: LocalDate)
    suspend fun addStreakFreezes(amount: Int)
    suspend fun useStreakFreeze(): Boolean

    // Achievements
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(id: String)
    suspend fun getUnlockedAchievementCount(): Int
    suspend fun checkAndUnlockAchievements()
}
