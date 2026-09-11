package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.data.local.dao.ExpenseDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(profileId: String): Flow<List<Expense>> {
        return expenseDao.getAllExpensesForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(profileId: String, id: String): Expense? {
        return expenseDao.getExpenseById(profileId, id)?.toDomain()
    }

    override fun getRecentExpenses(profileId: String, limit: Int): Flow<List<Expense>> {
        return expenseDao.getRecentExpensesForProfile(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpensesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDates(profileId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun filterExpenses(
        profileId: String,
        query: String?,
        categoryId: String?,
        paymentMethodId: String?,
        startDate: Long?,
        endDate: Long?,
        sortOrder: String
    ): Flow<List<Expense>> {
        val cleanQuery = if (query.isNullOrBlank()) null else query.trim()
        return expenseDao.filterExpenses(
            profileId = profileId,
            query = cleanQuery,
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            startDate = startDate,
            endDate = endDate,
            sortOrder = sortOrder
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategorySpending(profileId: String, startDate: Long, endDate: Long): Flow<List<CategorySpendAggregate>> {
        return expenseDao.getCategorySpendingBetween(profileId, startDate, endDate)
    }

    override fun getTotalSpending(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return expenseDao.getTotalSpendingBetween(profileId, startDate, endDate).map { it ?: BigDecimal.ZERO }
    }

    override fun getCategoryTotalSpending(profileId: String, categoryId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return expenseDao.getCategoryTotalSpendingBetween(profileId, categoryId, startDate, endDate).map { it ?: BigDecimal.ZERO }
    }

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(profileId: String, id: String) {
        expenseDao.deleteExpenseById(profileId, id)
    }
}
