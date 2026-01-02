package com.incremax.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.incremax.data.local.dao.*
import com.incremax.data.local.entity.*

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        WorkoutSessionEntity::class,
        UserStatsEntity::class,
        AchievementEntity::class,
        ExerciseTotalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IncremaxDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        const val DATABASE_NAME = "incremax_database"
    }
}
