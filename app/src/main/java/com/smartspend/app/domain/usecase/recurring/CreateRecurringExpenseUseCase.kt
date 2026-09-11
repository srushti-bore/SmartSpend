package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class CreateRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        profileId: String,
        title: String,
        amount: BigDecimal,
        frequency: RecurringFrequency,
        categoryId: String,
        paymentMethodId: String,
        startDate: Long = System.currentTimeMillis(),
        currency: String = "INR",
        autoCreate: Boolean = true,
        notifyBeforeDays: Int = 1,
        notes: String? = null
    ): Result<RecurringExpense> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Subscription/Expense title is required"))
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val category = categoryRepository.getCategoryById(profileId, categoryId)
            ?: return Result.failure(IllegalArgumentException("Invalid category selected"))

        val paymentMethod = paymentMethodRepository.getPaymentMethodById(profileId, paymentMethodId)
            ?: return Result.failure(IllegalArgumentException("Invalid payment method selected"))

        val recurring = RecurringExpense(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            title = trimmedTitle,
            amount = amount,
            currency = currency,
            categoryId = category.id,
            paymentMethodId = paymentMethod.id,
            frequency = frequency,
            startDate = startDate,
            nextDueDate = startDate,
            lastGeneratedDate = null,
            isActive = true,
            autoCreate = autoCreate,
            notifyBeforeDays = notifyBeforeDays,
            notes = notes?.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        recurringExpenseRepository.createRecurringExpense(recurring)
        return Result.success(recurring)
    }
}
