package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import java.math.BigDecimal
import javax.inject.Inject

class UpdateRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository
) {
    suspend operator fun invoke(recurringExpense: RecurringExpense): Result<RecurringExpense> {
        val trimmedTitle = recurringExpense.title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Subscription/Expense title is required"))
        }
        if (recurringExpense.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val updated = recurringExpense.copy(
            title = trimmedTitle,
            updatedAt = System.currentTimeMillis()
        )
        recurringExpenseRepository.updateRecurringExpense(updated)
        return Result.success(updated)
    }
}
