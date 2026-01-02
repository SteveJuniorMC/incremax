package com.incremax.domain.repository

import com.incremax.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getCustomExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: String): Exercise?
    suspend fun insertExercise(exercise: Exercise)
    suspend fun deleteExercise(id: String)
    suspend fun getExerciseTotal(exerciseId: String): Long
    fun getAllExerciseTotals(): Flow<Map<String, Long>>
    suspend fun addToExerciseTotal(exerciseId: String, amount: Int)
}
