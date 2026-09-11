package com.smartspend.app.feature.savingsgoal

import com.smartspend.app.core.common.Resource
import com.smartspend.app.domain.model.SavingsGoal

data class SavingsGoalUiState(
    val goalsResource: Resource<List<SavingsGoal>> = Resource.Loading,
    val isAddDialogOpen: Boolean = false,
    val contributeGoalTarget: SavingsGoal? = null,
    val isSubmitting: Boolean = false,
    val userErrorMessage: String? = null
)
