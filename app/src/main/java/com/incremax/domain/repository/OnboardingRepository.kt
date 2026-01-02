package com.incremax.domain.repository

import com.incremax.domain.model.FitnessGoal
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun hasCompletedOnboarding(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setSelectedGoal(goal: FitnessGoal)
    fun getSelectedGoal(): Flow<FitnessGoal?>
}
