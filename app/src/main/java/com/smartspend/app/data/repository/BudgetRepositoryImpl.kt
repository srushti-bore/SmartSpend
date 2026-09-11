package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.BudgetDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(profileId: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBudget(profileId: String, type: BudgetType, categoryId: String?): Budget? {
        return budgetDao.getBudget(profileId, type, categoryId)?.toDomain()
    }

    override fun getOverallBudgetFlow(profileId: String, type: BudgetType): Flow<Budget?> {
        return budgetDao.getOverallBudgetFlow(profileId, type).map { it?.toDomain() }
    }

    override suspend fun upsertBudget(budget: Budget) {
        budgetDao.upsertBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(profileId: String, id: String) {
        budgetDao.deleteBudgetById(profileId, id)
    }
}
