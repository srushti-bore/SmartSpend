package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.repository.RecurringExpenseRepository
import javax.inject.Inject

class DeleteRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository
) {
    suspend operator fun invoke(profileId: String, recurringExpenseId: String): Result<Unit> {
        val existing = recurringExpenseRepository.getRecurringExpenseById(profileId, recurringExpenseId)
            ?: return Result.failure(IllegalArgumentException("Recurring expense not found"))

        recurringExpenseRepository.deleteRecurringExpense(profileId, existing.id)
        return Result.success(Unit)
    }
}
