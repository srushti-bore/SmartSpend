package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(
        profileId: String,
        query: String? = null,
        categoryId: String? = null,
        paymentMethodId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        sortOrder: String = "DATE_DESC"
    ): Flow<List<Expense>> {
        return expenseRepository.filterExpenses(
            profileId = profileId,
            query = query,
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            startDate = startDate,
            endDate = endDate,
            sortOrder = sortOrder
        )
    }
}
