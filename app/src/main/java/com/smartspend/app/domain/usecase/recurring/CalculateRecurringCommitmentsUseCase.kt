package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateRecurringCommitmentsUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository
) {
    operator fun invoke(profileId: String): Flow<BigDecimal> {
        return recurringExpenseRepository.getActiveRecurringExpenses(profileId).map { list ->
            list.fold(BigDecimal.ZERO) { acc, item ->
                val monthlyNormalized = when (item.frequency) {
                    RecurringFrequency.DAILY -> item.amount.multiply(BigDecimal("30"))
                    RecurringFrequency.WEEKLY -> item.amount.multiply(BigDecimal("52")).divide(BigDecimal("12"), 2, RoundingMode.HALF_EVEN)
                    RecurringFrequency.MONTHLY -> item.amount
                    RecurringFrequency.YEARLY -> item.amount.divide(BigDecimal("12"), 2, RoundingMode.HALF_EVEN)
                }
                acc.add(monthlyNormalized)
            }
        }
    }
}
