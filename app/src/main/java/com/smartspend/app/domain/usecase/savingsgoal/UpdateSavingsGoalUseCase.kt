package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.repository.SavingsGoalRepository
import java.math.BigDecimal
import javax.inject.Inject

class UpdateSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(goal: SavingsGoal): Result<SavingsGoal> {
        val trimmedName = goal.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Goal name is required"))
        }
        if (goal.targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Target amount must be greater than zero"))
        }

        val updated = goal.copy(
            name = trimmedName,
            updatedAt = System.currentTimeMillis()
        )
        savingsGoalRepository.updateSavingsGoal(updated)
        return Result.success(updated)
    }
}
