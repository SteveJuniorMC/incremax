package com.incremax.data.repository

import com.incremax.data.local.dao.WorkoutPlanDao
import com.incremax.data.local.entity.WorkoutPlanEntity
import com.incremax.domain.model.WorkoutPlan
import com.incremax.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutPlanRepositoryImpl @Inject constructor(
    private val workoutPlanDao: WorkoutPlanDao
) : WorkoutPlanRepository {

    override fun getAllPlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getAllPlans().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActivePlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getActivePlans().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedPlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getCompletedPlans().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlanById(id: String): WorkoutPlan? {
        return workoutPlanDao.getPlanById(id)?.toDomain()
    }

    override fun getPlanByIdFlow(id: String): Flow<WorkoutPlan?> {
        return workoutPlanDao.getPlanByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertPlan(plan: WorkoutPlan) {
        workoutPlanDao.insertPlan(WorkoutPlanEntity.fromDomain(plan))
    }

    override suspend fun updatePlan(plan: WorkoutPlan) {
        workoutPlanDao.updatePlan(WorkoutPlanEntity.fromDomain(plan))
    }

    override suspend fun deletePlan(id: String) {
        workoutPlanDao.deletePlanById(id)
    }

    override suspend fun setActive(id: String, isActive: Boolean) {
        workoutPlanDao.setActive(id, isActive)
    }

    override suspend fun getCompletedPlansCount(): Int {
        return workoutPlanDao.getCompletedPlansCount()
    }
}
