package com.incremax.data.local.dao

import androidx.room.*
import com.incremax.data.local.entity.AchievementEntity
import com.incremax.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_stats SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE user_stats SET currentStreak = :streak WHERE id = 1")
    suspend fun updateStreak(streak: Int)

    @Query("UPDATE user_stats SET longestStreak = :streak WHERE id = 1")
    suspend fun updateLongestStreak(streak: Int)

    @Query("UPDATE user_stats SET totalWorkouts = totalWorkouts + 1 WHERE id = 1")
    suspend fun incrementWorkouts()

    @Query("UPDATE user_stats SET lastWorkoutDate = :date WHERE id = 1")
    suspend fun updateLastWorkoutDate(date: LocalDate)

    @Query("UPDATE user_stats SET streakFreezes = streakFreezes + :amount WHERE id = 1")
    suspend fun addStreakFreezes(amount: Int)

    @Query("UPDATE user_stats SET streakFreezes = streakFreezes - 1 WHERE id = 1 AND streakFreezes > 0")
    suspend fun useStreakFreeze()

    @Query("DELETE FROM user_stats")
    suspend fun deleteStats()

    @Query("DELETE FROM achievements")
    suspend fun deleteAchievements()

    @Query("DELETE FROM exercise_totals")
    suspend fun deleteExerciseTotals()

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsSync(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedAchievementCount(): Int
}
