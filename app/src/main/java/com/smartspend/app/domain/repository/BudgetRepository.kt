package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(profileId: String): Flow<List<Budget>>
    suspend fun getBudget(profileId: String, type: BudgetType, categoryId: String?): Budget?
    fun getOverallBudgetFlow(profileId: String, type: BudgetType): Flow<Budget?>
    fun getOverallBudget(profileId: String): Flow<Budget?> = getOverallBudgetFlow(profileId, BudgetType.MONTHLY)
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(profileId: String, id: String)
}
