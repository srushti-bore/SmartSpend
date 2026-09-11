package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.RecurringExpense
import kotlinx.coroutines.flow.Flow

interface RecurringExpenseRepository {
    fun getAllRecurringExpenses(profileId: String): Flow<List<RecurringExpense>>
    fun getActiveRecurringExpenses(profileId: String): Flow<List<RecurringExpense>>
    suspend fun getRecurringExpenseById(profileId: String, id: String): RecurringExpense?
    suspend fun getDueRecurringExpenses(cutoffDate: Long): List<RecurringExpense>
    suspend fun createRecurringExpense(recurringExpense: RecurringExpense)
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)
    suspend fun deleteRecurringExpense(profileId: String, id: String)
}
