package com.smartspend.app.domain.usecase.savingsgoal

import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.model.SavingsGoalContribution
import com.smartspend.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SavingsGoalDetails(
    val goal: SavingsGoal,
    val contributions: List<SavingsGoalContribution>
)

class GetSavingsGoalDetailsUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    operator fun invoke(profileId: String, goalId: String): Flow<SavingsGoalDetails?> {
        return savingsGoalRepository.getContributionsForGoal(profileId, goalId).map { contributions ->
            val goal = savingsGoalRepository.getSavingsGoalById(profileId, goalId)
            goal?.let {
                SavingsGoalDetails(
                    goal = it,
                    contributions = contributions
                )
            }
        }
    }
}
