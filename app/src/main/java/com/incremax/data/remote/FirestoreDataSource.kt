package com.incremax.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.incremax.data.local.entity.*
import com.incremax.domain.model.ExerciseCategory
import com.incremax.domain.model.ExerciseType
import com.incremax.domain.model.IncrementFrequency
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(userId: String) = firestore.collection("users").document(userId)

    // Stats
    suspend fun uploadStats(userId: String, stats: UserStatsEntity) {
        userDoc(userId).collection("stats").document("current")
            .set(stats.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun downloadStats(userId: String): UserStatsEntity? {
        val doc = userDoc(userId).collection("stats").document("current").get().await()
        return doc.data?.toUserStatsEntity()
    }

    // Exercises (custom only)
    suspend fun uploadExercises(userId: String, exercises: List<ExerciseEntity>) {
        val customExercises = exercises.filter { it.isCustom }
        if (customExercises.isEmpty()) return

        val batch = firestore.batch()
        customExercises.forEach { exercise ->
            val ref = userDoc(userId).collection("exercises").document(exercise.id)
            batch.set(ref, exercise.toFirestoreMap())
        }
        batch.commit().await()
    }

    suspend fun downloadExercises(userId: String): List<ExerciseEntity> {
        val snapshot = userDoc(userId).collection("exercises").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toExerciseEntity(doc.id)
        }
    }

    // Workout Plans
    suspend fun uploadPlans(userId: String, plans: List<WorkoutPlanEntity>) {
        if (plans.isEmpty()) return

        val batch = firestore.batch()
        plans.forEach { plan ->
            val ref = userDoc(userId).collection("workout_plans").document(plan.id)
            batch.set(ref, plan.toFirestoreMap())
        }
        batch.commit().await()
    }

    suspend fun downloadPlans(userId: String): List<WorkoutPlanEntity> {
        val snapshot = userDoc(userId).collection("workout_plans").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toWorkoutPlanEntity(doc.id)
        }
    }

    // Workout Sessions
    suspend fun uploadSessions(userId: String, sessions: List<WorkoutSessionEntity>) {
        if (sessions.isEmpty()) return

        // Firestore batch limit is 500, so chunk if needed
        sessions.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { session ->
                val ref = userDoc(userId).collection("workout_sessions").document(session.id)
                batch.set(ref, session.toFirestoreMap())
            }
            batch.commit().await()
        }
    }

    suspend fun downloadSessions(userId: String): List<WorkoutSessionEntity> {
        val snapshot = userDoc(userId).collection("workout_sessions").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toWorkoutSessionEntity(doc.id)
        }
    }

    // Achievements
    suspend fun uploadAchievements(userId: String, achievements: List<AchievementEntity>) {
        val unlockedAchievements = achievements.filter { it.unlockedAt != null }
        if (unlockedAchievements.isEmpty()) return

        val batch = firestore.batch()
        unlockedAchievements.forEach { achievement ->
            val ref = userDoc(userId).collection("achievements").document(achievement.id)
            batch.set(ref, achievement.toFirestoreMap())
        }
        batch.commit().await()
    }

    suspend fun downloadAchievements(userId: String): List<AchievementEntity> {
        val snapshot = userDoc(userId).collection("achievements").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toAchievementEntity(doc.id)
        }
    }

    // Exercise Totals
    suspend fun uploadExerciseTotals(userId: String, totals: List<ExerciseTotalEntity>) {
        if (totals.isEmpty()) return

        val batch = firestore.batch()
        totals.forEach { total ->
            val ref = userDoc(userId).collection("exercise_totals").document(total.exerciseId)
            batch.set(ref, total.toFirestoreMap())
        }
        batch.commit().await()
    }

    suspend fun downloadExerciseTotals(userId: String): List<ExerciseTotalEntity> {
        val snapshot = userDoc(userId).collection("exercise_totals").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toExerciseTotalEntity(doc.id)
        }
    }

    // Check if user has any cloud data
    suspend fun hasData(userId: String): Boolean {
        val stats = userDoc(userId).collection("stats").document("current").get().await()
        return stats.exists()
    }

    // Update last sync timestamp
    suspend fun updateLastSync(userId: String) {
        userDoc(userId).set(
            mapOf("lastSyncAt" to System.currentTimeMillis()),
            SetOptions.merge()
        ).await()
    }

    // Companion object with date formatters
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
        private val DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME
    }

    // Entity to Firestore map conversions
    private fun UserStatsEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "totalXp" to totalXp,
        "level" to level,
        "currentStreak" to currentStreak,
        "longestStreak" to longestStreak,
        "totalWorkouts" to totalWorkouts,
        "streakFreezes" to streakFreezes,
        "lastWorkoutDate" to lastWorkoutDate?.format(DATE_FORMATTER)
    )

    private fun Map<String, Any?>.toUserStatsEntity(): UserStatsEntity = UserStatsEntity(
        id = 1,
        totalXp = (this["totalXp"] as? Long)?.toInt() ?: 0,
        level = (this["level"] as? Long)?.toInt() ?: 1,
        currentStreak = (this["currentStreak"] as? Long)?.toInt() ?: 0,
        longestStreak = (this["longestStreak"] as? Long)?.toInt() ?: 0,
        totalWorkouts = (this["totalWorkouts"] as? Long)?.toInt() ?: 0,
        streakFreezes = (this["streakFreezes"] as? Long)?.toInt() ?: 0,
        lastWorkoutDate = (this["lastWorkoutDate"] as? String)?.let {
            LocalDate.parse(it, DATE_FORMATTER)
        }
    )

    private fun ExerciseEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "type" to type.name,
        "category" to category.name,
        "unit" to unit,
        "icon" to icon,
        "description" to description,
        "isCustom" to isCustom
    )

    private fun Map<String, Any?>.toExerciseEntity(id: String): ExerciseEntity = ExerciseEntity(
        id = id,
        name = this["name"] as? String ?: "",
        type = (this["type"] as? String)?.let { ExerciseType.valueOf(it) } ?: ExerciseType.REPS,
        category = (this["category"] as? String)?.let { ExerciseCategory.valueOf(it) } ?: ExerciseCategory.BODYWEIGHT,
        unit = this["unit"] as? String ?: "",
        icon = this["icon"] as? String ?: "",
        description = this["description"] as? String ?: "",
        isCustom = this["isCustom"] as? Boolean ?: true
    )

    private fun WorkoutPlanEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "exerciseId" to exerciseId,
        "startingAmount" to startingAmount,
        "targetAmount" to targetAmount,
        "incrementAmount" to incrementAmount,
        "incrementFrequency" to incrementFrequency.name,
        "startDate" to startDate.format(DATE_FORMATTER),
        "isActive" to isActive,
        "isPreset" to isPreset,
        "completedDate" to completedDate?.format(DATE_FORMATTER),
        "reminderEnabled" to reminderEnabled,
        "reminderTime" to reminderTime?.format(TIME_FORMATTER)
    )

    private fun Map<String, Any?>.toWorkoutPlanEntity(id: String): WorkoutPlanEntity = WorkoutPlanEntity(
        id = id,
        name = this["name"] as? String ?: "",
        description = this["description"] as? String ?: "",
        exerciseId = this["exerciseId"] as? String ?: "",
        startingAmount = (this["startingAmount"] as? Long)?.toInt() ?: 0,
        targetAmount = (this["targetAmount"] as? Long)?.toInt() ?: 0,
        incrementAmount = (this["incrementAmount"] as? Long)?.toInt() ?: 0,
        incrementFrequency = (this["incrementFrequency"] as? String)?.let {
            IncrementFrequency.valueOf(it)
        } ?: IncrementFrequency.WEEKLY,
        startDate = (this["startDate"] as? String)?.let {
            LocalDate.parse(it, DATE_FORMATTER)
        } ?: LocalDate.now(),
        isActive = this["isActive"] as? Boolean ?: false,
        isPreset = this["isPreset"] as? Boolean ?: false,
        completedDate = (this["completedDate"] as? String)?.let {
            LocalDate.parse(it, DATE_FORMATTER)
        },
        reminderEnabled = this["reminderEnabled"] as? Boolean ?: false,
        reminderTime = (this["reminderTime"] as? String)?.let {
            LocalTime.parse(it, TIME_FORMATTER)
        }
    )

    private fun WorkoutSessionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "planId" to planId,
        "exerciseId" to exerciseId,
        "date" to date.format(DATE_FORMATTER),
        "completedAmount" to completedAmount,
        "targetAmount" to targetAmount,
        "xpEarned" to xpEarned,
        "durationSeconds" to durationSeconds,
        "completedAt" to completedAt.format(DATETIME_FORMATTER)
    )

    private fun Map<String, Any?>.toWorkoutSessionEntity(id: String): WorkoutSessionEntity = WorkoutSessionEntity(
        id = id,
        planId = this["planId"] as? String ?: "",
        exerciseId = this["exerciseId"] as? String ?: "",
        date = (this["date"] as? String)?.let {
            LocalDate.parse(it, DATE_FORMATTER)
        } ?: LocalDate.now(),
        completedAmount = (this["completedAmount"] as? Long)?.toInt() ?: 0,
        targetAmount = (this["targetAmount"] as? Long)?.toInt() ?: 0,
        xpEarned = (this["xpEarned"] as? Long)?.toInt() ?: 0,
        durationSeconds = (this["durationSeconds"] as? Long) ?: 0L,
        completedAt = (this["completedAt"] as? String)?.let {
            LocalDateTime.parse(it, DATETIME_FORMATTER)
        } ?: LocalDateTime.now()
    )

    private fun AchievementEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "unlockedAt" to unlockedAt?.format(DATETIME_FORMATTER)
    )

    private fun Map<String, Any?>.toAchievementEntity(id: String): AchievementEntity = AchievementEntity(
        id = id,
        unlockedAt = (this["unlockedAt"] as? String)?.let {
            LocalDateTime.parse(it, DATETIME_FORMATTER)
        }
    )

    private fun ExerciseTotalEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "totalAmount" to totalAmount
    )

    private fun Map<String, Any?>.toExerciseTotalEntity(id: String): ExerciseTotalEntity = ExerciseTotalEntity(
        exerciseId = id,
        totalAmount = (this["totalAmount"] as? Long) ?: 0L
    )
}
