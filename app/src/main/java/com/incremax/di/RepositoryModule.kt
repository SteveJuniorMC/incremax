package com.incremax.di

import com.incremax.data.repository.*
import com.incremax.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutPlanRepository(
        impl: WorkoutPlanRepositoryImpl
    ): WorkoutPlanRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(
        impl: WorkoutSessionRepositoryImpl
    ): WorkoutSessionRepository

    @Binds
    @Singleton
    abstract fun bindUserStatsRepository(
        impl: UserStatsRepositoryImpl
    ): UserStatsRepository
}
