package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.SavingsGoalContributionDao
import com.smartspend.app.data.local.dao.SavingsGoalDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.model.SavingsGoalContribution
import com.smartspend.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsGoalRepositoryImpl @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao,
    private val savingsGoalContributionDao: SavingsGoalContributionDao
) : SavingsGoalRepository {

    override fun getActiveSavingsGoals(profileId: String): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getActiveSavingsGoals(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllSavingsGoals(profileId: String): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAllSavingsGoals(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSavingsGoalById(profileId: String, id: String): SavingsGoal? {
        return savingsGoalDao.getSavingsGoalById(profileId, id)?.toDomain()
    }

    override suspend fun createSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.insertSavingsGoal(goal.toEntity())
    }

    override suspend fun updateSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.updateSavingsGoal(goal.toEntity())
    }

    override suspend fun deleteSavingsGoal(profileId: String, id: String) {
        savingsGoalDao.deleteSavingsGoalById(profileId, id)
    }

    override fun getContributionsForGoal(profileId: String, goalId: String): Flow<List<SavingsGoalContribution>> {
        return savingsGoalContributionDao.getContributionsForGoal(profileId, goalId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addContribution(contribution: SavingsGoalContribution) {
        savingsGoalContributionDao.insertContribution(contribution.toEntity())
    }

    override suspend fun deleteContribution(profileId: String, id: String) {
        savingsGoalContributionDao.deleteContributionById(profileId, id)
    }
}
