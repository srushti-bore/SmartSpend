package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(profileId: String, expenseId: String) {
        expenseRepository.deleteExpense(profileId, expenseId)
    }
}
