package com.incremax.data.repository

import com.incremax.data.local.dao.WorkoutSessionDao
import com.incremax.data.local.entity.WorkoutSessionEntity
import com.incremax.domain.model.WorkoutSession
import com.incremax.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class WorkoutSessionRepositoryImpl @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao
) : WorkoutSessionRepository {

    override fun getAllSessions(): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionsByPlan(planId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getSessionsByPlan(planId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionsByDate(date: LocalDate): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getSessionsByDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSessionsByDateSync(date: LocalDate): List<WorkoutSession> {
        return workoutSessionDao.getSessionsByDateSync(date).map { it.toDomain() }
    }

    override fun getSessionsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getSessionsInRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSessionById(id: String): WorkoutSession? {
        return workoutSessionDao.getSessionById(id)?.toDomain()
    }

    override suspend fun getSessionForPlanOnDate(planId: String, date: LocalDate): WorkoutSession? {
        return workoutSessionDao.getSessionForPlanOnDate(planId, date)?.toDomain()
    }

    override suspend fun insertSession(session: WorkoutSession) {
        workoutSessionDao.insertSession(WorkoutSessionEntity.fromDomain(session))
    }

    override suspend fun updateSession(session: WorkoutSession) {
        workoutSessionDao.updateSession(WorkoutSessionEntity.fromDomain(session))
    }

    override suspend fun deleteSession(session: WorkoutSession) {
        workoutSessionDao.deleteSession(WorkoutSessionEntity.fromDomain(session))
    }

    override suspend fun getTotalSessionCount(): Int {
        return workoutSessionDao.getTotalSessionCount()
    }

    override suspend fun getTotalXpEarned(): Int {
        return workoutSessionDao.getTotalXpEarned() ?: 0
    }

    override fun getWorkoutDates(): Flow<List<LocalDate>> {
        return workoutSessionDao.getWorkoutDates()
    }

    override suspend fun getUniqueDaysWorkedOut(): Int {
        return workoutSessionDao.getUniqueDaysWorkedOut()
    }

    override suspend fun getPersonalRecord(exerciseId: String): WorkoutSession? {
        return workoutSessionDao.getPersonalRecord(exerciseId)?.toDomain()
    }

    override suspend fun getTotalForExercise(exerciseId: String): Long {
        return workoutSessionDao.getTotalForExercise(exerciseId) ?: 0L
    }
}
