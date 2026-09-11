package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import java.math.BigDecimal
import javax.inject.Inject

class EditExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        profileId: String,
        expenseId: String,
        title: String,
        amount: BigDecimal,
        currency: String,
        categoryId: String,
        paymentMethodId: String,
        date: Long,
        notes: String?,
        isRecurring: Boolean
    ): Result<Expense> {
        val existing = expenseRepository.getExpenseById(profileId, expenseId)
            ?: return Result.failure(IllegalArgumentException("Expense not found"))

        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Expense title is required"))
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        val currentTime = System.currentTimeMillis()
        if (date > currentTime + 300000L) {
            return Result.failure(IllegalArgumentException("Future date transactions are not allowed"))
        }

        val category = categoryRepository.getCategoryById(profileId, categoryId)
            ?: return Result.failure(IllegalArgumentException("Invalid category selected"))

        val paymentMethod = paymentMethodRepository.getPaymentMethodById(profileId, paymentMethodId)
            ?: return Result.failure(IllegalArgumentException("Invalid payment method selected"))

        val updated = existing.copy(
            title = trimmedTitle,
            amount = amount,
            currency = currency,
            categoryId = category.id,
            paymentMethodId = paymentMethod.id,
            date = date,
            notes = notes?.trim(),
            isRecurring = isRecurring,
            updatedAt = System.currentTimeMillis()
        )

        expenseRepository.updateExpense(updated)
        return Result.success(updated)
    }
}
