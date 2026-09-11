package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.ContributionType
import com.smartspend.app.domain.model.SavingsGoalContribution
import com.smartspend.app.domain.repository.SavingsGoalRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class ContributeToSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(
        profileId: String,
        goalId: String,
        amount: BigDecimal,
        type: ContributionType = ContributionType.DEPOSIT,
        notes: String? = null,
        date: Long = System.currentTimeMillis()
    ): Result<SavingsGoalContribution> {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val goal = savingsGoalRepository.getSavingsGoalById(profileId, goalId)
            ?: return Result.failure(IllegalArgumentException("Savings goal not found"))

        val newCurrentAmount = when (type) {
            ContributionType.DEPOSIT -> goal.currentAmount.add(amount)
            ContributionType.WITHDRAWAL -> {
                if (goal.currentAmount < amount) {
                    return Result.failure(IllegalArgumentException("Withdrawal amount cannot exceed current saved amount"))
                }
                goal.currentAmount.subtract(amount)
            }
        }

        val contribution = SavingsGoalContribution(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            profileId = profileId,
            amount = amount,
            type = type,
            date = date,
            notes = notes?.trim(),
            createdAt = System.currentTimeMillis()
        )

        savingsGoalRepository.addContribution(contribution)
        savingsGoalRepository.updateSavingsGoal(
            goal.copy(
                currentAmount = newCurrentAmount,
                updatedAt = System.currentTimeMillis()
            )
        )

        return Result.success(contribution)
    }
}
