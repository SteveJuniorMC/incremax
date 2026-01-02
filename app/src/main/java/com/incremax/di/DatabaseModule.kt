package com.incremax.di

import android.content.Context
import androidx.room.Room
import com.incremax.data.local.dao.*
import com.incremax.data.local.database.IncremaxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): IncremaxDatabase {
        return Room.databaseBuilder(
            context,
            IncremaxDatabase::class.java,
            IncremaxDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: IncremaxDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutPlanDao(database: IncremaxDatabase): WorkoutPlanDao {
        return database.workoutPlanDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutSessionDao(database: IncremaxDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    @Provides
    @Singleton
    fun provideUserStatsDao(database: IncremaxDatabase): UserStatsDao {
        return database.userStatsDao()
    }
}
