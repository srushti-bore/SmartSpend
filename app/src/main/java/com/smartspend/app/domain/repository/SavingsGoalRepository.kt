package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.model.SavingsGoalContribution
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getActiveSavingsGoals(profileId: String): Flow<List<SavingsGoal>>
    fun getAllSavingsGoals(profileId: String): Flow<List<SavingsGoal>>
    suspend fun getSavingsGoalById(profileId: String, id: String): SavingsGoal?
    suspend fun createSavingsGoal(goal: SavingsGoal)
    suspend fun updateSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(profileId: String, id: String)
    fun getContributionsForGoal(profileId: String, goalId: String): Flow<List<SavingsGoalContribution>>
    suspend fun addContribution(contribution: SavingsGoalContribution)
    suspend fun deleteContribution(profileId: String, id: String)
}
