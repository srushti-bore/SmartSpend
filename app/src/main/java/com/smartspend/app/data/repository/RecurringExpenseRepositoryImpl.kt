package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.RecurringExpenseDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseRepositoryImpl @Inject constructor(
    private val recurringExpenseDao: RecurringExpenseDao
) : RecurringExpenseRepository {

    override fun getAllRecurringExpenses(profileId: String): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getAllRecurringExpenses(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveRecurringExpenses(profileId: String): Flow<List<RecurringExpense>> {
        return recurringExpenseDao.getActiveRecurringExpenses(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecurringExpenseById(profileId: String, id: String): RecurringExpense? {
        return recurringExpenseDao.getRecurringExpenseById(profileId, id)?.toDomain()
    }

    override suspend fun getDueRecurringExpenses(cutoffDate: Long): List<RecurringExpense> {
        return recurringExpenseDao.getDueRecurringExpenses(cutoffDate).map { it.toDomain() }
    }

    override suspend fun createRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.insertRecurringExpense(recurringExpense.toEntity())
    }

    override suspend fun updateRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.updateRecurringExpense(recurringExpense.toEntity())
    }

    override suspend fun deleteRecurringExpense(profileId: String, id: String) {
        recurringExpenseDao.deleteRecurringExpenseById(profileId, id)
    }
}
