package com.incremax.data.repository

import com.incremax.data.local.dao.ExerciseDao
import com.incremax.data.local.entity.ExerciseEntity
import com.incremax.domain.model.Exercise
import com.incremax.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCustomExercises(): Flow<List<Exercise>> {
        return exerciseDao.getCustomExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomain()
    }

    override suspend fun insertExercise(exercise: Exercise) {
        exerciseDao.insertExercise(ExerciseEntity.fromDomain(exercise))
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteExerciseById(id)
    }

    override suspend fun getExerciseTotal(exerciseId: String): Long {
        return exerciseDao.getExerciseTotal(exerciseId)?.totalAmount ?: 0L
    }

    override fun getAllExerciseTotals(): Flow<Map<String, Long>> {
        return exerciseDao.getAllExerciseTotals().map { totals ->
            totals.associate { it.exerciseId to it.totalAmount }
        }
    }

    override suspend fun addToExerciseTotal(exerciseId: String, amount: Int) {
        exerciseDao.addToExerciseTotal(exerciseId, amount)
    }
}
