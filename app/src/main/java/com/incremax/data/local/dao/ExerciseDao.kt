package com.incremax.data.local.dao

import androidx.room.*
import com.incremax.data.local.entity.ExerciseEntity
import com.incremax.data.local.entity.ExerciseTotalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE isCustom = 1")
    fun getCustomExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: String)

    // Exercise totals
    @Query("SELECT * FROM exercise_totals WHERE exerciseId = :exerciseId")
    suspend fun getExerciseTotal(exerciseId: String): ExerciseTotalEntity?

    @Query("SELECT * FROM exercise_totals")
    fun getAllExerciseTotals(): Flow<List<ExerciseTotalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateExerciseTotal(total: ExerciseTotalEntity)

    @Query("UPDATE exercise_totals SET totalAmount = totalAmount + :amount WHERE exerciseId = :exerciseId")
    suspend fun incrementExerciseTotal(exerciseId: String, amount: Int)

    @Transaction
    suspend fun addToExerciseTotal(exerciseId: String, amount: Int) {
        val existing = getExerciseTotal(exerciseId)
        if (existing != null) {
            incrementExerciseTotal(exerciseId, amount)
        } else {
            updateExerciseTotal(ExerciseTotalEntity(exerciseId, amount.toLong()))
        }
    }
}
