package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        profileId: String,
        title: String,
        amount: BigDecimal,
        currency: String = "INR",
        categoryId: String,
        paymentMethodId: String,
        date: Long,
        notes: String? = null,
        isRecurring: Boolean = false,
        source: ExpenseSource = ExpenseSource.MANUAL,
        attachmentRef: String? = null
    ): Result<Expense> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Expense title is required"))
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        val currentTime = System.currentTimeMillis()
        // Allow up to a 5-minute buffer for clock skew, but reject future dates per SRS §16.1
        if (date > currentTime + 300000L) {
            return Result.failure(IllegalArgumentException("Future date transactions are not allowed"))
        }

        val category = categoryRepository.getCategoryById(profileId, categoryId)
            ?: return Result.failure(IllegalArgumentException("Invalid category selected"))

        val paymentMethod = paymentMethodRepository.getPaymentMethodById(profileId, paymentMethodId)
            ?: return Result.failure(IllegalArgumentException("Invalid payment method selected"))

        val expense = Expense(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            title = trimmedTitle,
            amount = amount,
            currency = currency,
            categoryId = category.id,
            paymentMethodId = paymentMethod.id,
            date = date,
            notes = notes?.trim(),
            isRecurring = isRecurring,
            source = source,
            attachmentRef = attachmentRef,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        expenseRepository.addExpense(expense)
        return Result.success(expense)
    }
}
