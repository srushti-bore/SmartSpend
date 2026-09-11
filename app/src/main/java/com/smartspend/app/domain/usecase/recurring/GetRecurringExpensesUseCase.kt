package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository
) {
    operator fun invoke(profileId: String, onlyActive: Boolean = true): Flow<List<RecurringExpense>> {
        return if (onlyActive) {
            recurringExpenseRepository.getActiveRecurringExpenses(profileId)
        } else {
            recurringExpenseRepository.getAllRecurringExpenses(profileId)
        }
    }
}
