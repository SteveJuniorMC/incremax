package com.incremax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.incremax.di.OnboardingDataStore
import com.incremax.domain.model.FitnessGoal
import com.incremax.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    @OnboardingDataStore private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_GOAL = stringPreferencesKey("selected_fitness_goal")
    }

    override fun hasCompletedOnboarding(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[ONBOARDING_COMPLETED] ?: false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setSelectedGoal(goal: FitnessGoal) {
        dataStore.edit { it[SELECTED_GOAL] = goal.name }
    }

    override fun getSelectedGoal(): Flow<FitnessGoal?> {
        return dataStore.data.map { prefs ->
            prefs[SELECTED_GOAL]?.let {
                try {
                    FitnessGoal.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}
