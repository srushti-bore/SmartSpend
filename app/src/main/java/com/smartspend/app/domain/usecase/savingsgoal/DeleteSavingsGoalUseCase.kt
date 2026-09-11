package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.repository.SavingsGoalRepository
import javax.inject.Inject

class DeleteSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(profileId: String, goalId: String): Result<Unit> {
        val existing = savingsGoalRepository.getSavingsGoalById(profileId, goalId)
            ?: return Result.failure(IllegalArgumentException("Savings goal not found"))

        savingsGoalRepository.deleteSavingsGoal(profileId, existing.id)
        return Result.success(Unit)
    }
}
