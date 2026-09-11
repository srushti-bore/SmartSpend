package com.smartspend.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.usecase.budget.CalculateBudgetProgressUseCase
import com.smartspend.app.domain.usecase.budget.GetBudgetsUseCase
import com.smartspend.app.domain.usecase.budget.UpsertBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class BudgetUiState(
    val budgetProgressList: List<BudgetProgress> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val newBudgetAmountInput: String = "",
    val newBudgetType: BudgetType = BudgetType.MONTHLY,
    val newBudgetThresholdPct: Int = 80,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val upsertBudgetUseCase: UpsertBudgetUseCase,
    private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
    private val budgetRepository: BudgetRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        observeBudgets()
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    getBudgetsUseCase(profileId).flatMapLatest { budgets ->
                        if (budgets.isEmpty()) {
                            flowOf(emptyList())
                        } else {
                            val progressFlows = budgets.map { calculateBudgetProgressUseCase(profileId, it) }
                            combine(progressFlows) { it.toList() }
                        }
                    }
                } else {
                    flowOf(emptyList())
                }
            }.collect { progressList ->
                _uiState.value = _uiState.value.copy(
                    budgetProgressList = progressList,
                    isLoading = false
                )
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = true, newBudgetAmountInput = "", errorMessage = null)
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = false, errorMessage = null)
    }

    fun onAmountInputChange(amount: String) {
        _uiState.value = _uiState.value.copy(newBudgetAmountInput = amount, errorMessage = null)
    }

    fun onTypeSelect(type: BudgetType) {
        _uiState.value = _uiState.value.copy(newBudgetType = type)
    }

    fun saveBudget() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return

        val parsedAmount = MoneyUtils.parse(state.newBudgetAmountInput)
        if (parsedAmount == null || parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid positive budget amount")
            return
        }

        viewModelScope.launch {
            val result = upsertBudgetUseCase(
                profileId = profileId,
                type = state.newBudgetType,
                amount = parsedAmount,
                thresholdPct = state.newBudgetThresholdPct
            )

            result.fold(
                onSuccess = {
                    closeAddDialog()
                },
                onFailure = { error ->
                    _uiState.value = state.copy(errorMessage = error.message ?: "Failed to save budget")
                }
            )
        }
    }

    fun deleteBudget(budgetId: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            budgetRepository.deleteBudget(profileId, budgetId)
        }
    }
}
