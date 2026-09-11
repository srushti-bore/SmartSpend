package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.repository.SavingsGoalRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class CreateSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(
        profileId: String,
        name: String,
        targetAmount: BigDecimal,
        targetDate: Long,
        currency: String = "INR",
        initialAmount: BigDecimal = BigDecimal.ZERO,
        color: String? = null,
        icon: String? = null
    ): Result<SavingsGoal> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Goal name is required"))
        }
        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Target amount must be greater than zero"))
        }
        val currentTime = System.currentTimeMillis()
        if (targetDate <= currentTime) {
            return Result.failure(IllegalArgumentException("Target date must be in the future"))
        }

        val goal = SavingsGoal(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            name = trimmedName,
            targetAmount = targetAmount,
            currentAmount = initialAmount.coerceAtLeast(BigDecimal.ZERO),
            currency = currency,
            targetDate = targetDate,
            color = color,
            icon = icon,
            isArchived = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        savingsGoalRepository.createSavingsGoal(goal)
        return Result.success(goal)
    }
}
