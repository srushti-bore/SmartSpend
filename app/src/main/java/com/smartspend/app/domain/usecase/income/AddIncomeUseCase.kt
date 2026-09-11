package com.smartspend.app.domain.usecase.income

import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.repository.IncomeRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    suspend operator fun invoke(
        profileId: String,
        title: String,
        amount: BigDecimal,
        source: IncomeSource,
        currency: String = "INR",
        date: Long = System.currentTimeMillis(),
        paymentMethodId: String? = null,
        notes: String? = null,
        isRecurring: Boolean = false
    ): Result<Income> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Income title is required"))
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Income amount must be greater than zero"))
        }
        val currentTime = System.currentTimeMillis()
        // Allow up to a 5-minute buffer for clock skew
        if (date > currentTime + 300000L) {
            return Result.failure(IllegalArgumentException("Future date income records are not allowed"))
        }

        val income = Income(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            source = source,
            title = trimmedTitle,
            amount = amount,
            currency = currency,
            date = date,
            paymentMethodId = paymentMethodId,
            notes = notes?.trim(),
            isRecurring = isRecurring,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        incomeRepository.addIncome(income)
        return Result.success(income)
    }
}
