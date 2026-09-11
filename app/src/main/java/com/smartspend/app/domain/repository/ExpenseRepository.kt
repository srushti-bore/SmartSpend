package com.smartspend.app.domain.repository

import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface ExpenseRepository {
    fun getAllExpenses(profileId: String): Flow<List<Expense>>
    suspend fun getExpenseById(profileId: String, id: String): Expense?
    fun getRecentExpenses(profileId: String, limit: Int = 5): Flow<List<Expense>>
    fun getExpensesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<Expense>>
    fun filterExpenses(
        profileId: String,
        query: String?,
        categoryId: String?,
        paymentMethodId: String?,
        startDate: Long?,
        endDate: Long?,
        sortOrder: String
    ): Flow<List<Expense>>
    fun getCategorySpending(profileId: String, startDate: Long, endDate: Long): Flow<List<CategorySpendAggregate>>
    fun getTotalSpending(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal>
    fun getCategoryTotalSpending(profileId: String, categoryId: String, startDate: Long, endDate: Long): Flow<BigDecimal>
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(profileId: String, id: String)
}
