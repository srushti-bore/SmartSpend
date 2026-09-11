package com.smartspend.app.feature.savingsgoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.common.Resource
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.ContributionType
import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.usecase.savingsgoal.ContributeToSavingsGoalUseCase
import com.smartspend.app.domain.usecase.savingsgoal.CreateSavingsGoalUseCase
import com.smartspend.app.domain.usecase.savingsgoal.DeleteSavingsGoalUseCase
import com.smartspend.app.domain.usecase.savingsgoal.GetSavingsGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val getSavingsGoalsUseCase: GetSavingsGoalsUseCase,
    private val createSavingsGoalUseCase: CreateSavingsGoalUseCase,
    private val contributeToSavingsGoalUseCase: ContributeToSavingsGoalUseCase,
    private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    observeGoals(profileId)
                }
            }
        }
    }

    private fun observeGoals(profileId: String) {
        viewModelScope.launch {
            getSavingsGoalsUseCase(profileId)
                .catch { e -> _uiState.update { it.copy(goalsResource = Resource.Error(e.message ?: "Failed to load savings goals")) } }
                .collect { goals ->
                    _uiState.update {
                        it.copy(
                            goalsResource = if (goals.isEmpty()) Resource.Empty else Resource.Success(goals)
                        )
                    }
                }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true, userErrorMessage = null) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, userErrorMessage = null) }
    }

    fun openContributeDialog(goal: SavingsGoal) {
        _uiState.update { it.copy(contributeGoalTarget = goal, userErrorMessage = null) }
    }

    fun closeContributeDialog() {
        _uiState.update { it.copy(contributeGoalTarget = null, userErrorMessage = null) }
    }

    fun createSavingsGoal(
        name: String,
        targetAmountStr: String,
        targetDateMs: Long,
        initialAmountStr: String
    ) {
        val profileId = activeProfileId ?: return
        val targetAmount = try {
            BigDecimal(targetAmountStr.trim())
        } catch (e: Exception) {
            _uiState.update { it.copy(userErrorMessage = "Invalid target amount") }
            return
        }

        val initialAmount = try {
            if (initialAmountStr.isBlank()) BigDecimal.ZERO else BigDecimal(initialAmountStr.trim())
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, userErrorMessage = null) }
            val result = createSavingsGoalUseCase(
                profileId = profileId,
                name = name,
                targetAmount = targetAmount,
                targetDate = targetDateMs,
                initialAmount = initialAmount
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, isAddDialogOpen = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, userErrorMessage = error.message) }
                }
            )
        }
    }

    fun contribute(amountStr: String, type: ContributionType, notes: String? = null) {
        val profileId = activeProfileId ?: return
        val goal = _uiState.value.contributeGoalTarget ?: return
        val amount = try {
            BigDecimal(amountStr.trim())
        } catch (e: Exception) {
            _uiState.update { it.copy(userErrorMessage = "Invalid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, userErrorMessage = null) }
            val result = contributeToSavingsGoalUseCase(
                profileId = profileId,
                goalId = goal.id,
                amount = amount,
                type = type,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, contributeGoalTarget = null) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, userErrorMessage = error.message) }
                }
            )
        }
    }

    fun deleteGoal(goalId: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            deleteSavingsGoalUseCase(profileId, goalId)
        }
    }
}
