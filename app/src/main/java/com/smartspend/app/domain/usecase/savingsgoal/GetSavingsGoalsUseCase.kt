package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavingsGoalsUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    operator fun invoke(profileId: String, onlyActive: Boolean = true): Flow<List<SavingsGoal>> {
        return if (onlyActive) {
            savingsGoalRepository.getActiveSavingsGoals(profileId)
        } else {
            savingsGoalRepository.getAllSavingsGoals(profileId)
        }
    }
}
