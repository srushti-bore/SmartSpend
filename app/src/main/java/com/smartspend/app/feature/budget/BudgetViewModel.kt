package com.smartspend.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.usecase.budget.CalculateBudgetProgressUseCase
import com.smartspend.app.domain.usecase.budget.GetBudgetsUseCase
import com.smartspend.app.domain.usecase.budget.UpsertBudgetUseCase
import com.smartspend.app.domain.usecase.category.GetCategoriesUseCase
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
    val categories: List<Category> = emptyList(),
    val selectedFilterType: BudgetType? = null,
    val isAddDialogOpen: Boolean = false,
    val newBudgetAmountInput: String = "",
    val newBudgetType: BudgetType = BudgetType.MONTHLY,
    val selectedCategoryId: String? = null,
    val newBudgetThresholdPct: Int = 80,
    val preferredCurrency: String = "INR",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val upsertBudgetUseCase: UpsertBudgetUseCase,
    private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
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
            preferencesManager.preferredCurrencyFlow.collect { currency ->
                _uiState.value = _uiState.value.copy(preferredCurrency = currency)
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    getCategoriesUseCase(profileId)
                } else {
                    flowOf(emptyList())
                }
            }.collect { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    selectedCategoryId = _uiState.value.selectedCategoryId ?: categories.firstOrNull()?.id
                )
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

    fun onFilterSelect(type: BudgetType?) {
        _uiState.value = _uiState.value.copy(selectedFilterType = type)
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(
            isAddDialogOpen = true,
            newBudgetAmountInput = "",
            newBudgetType = BudgetType.MONTHLY,
            selectedCategoryId = _uiState.value.categories.firstOrNull()?.id,
            errorMessage = null
        )
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

    fun onCategorySelect(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun saveBudget() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return

        val parsedAmount = MoneyUtils.parse(state.newBudgetAmountInput)
        if (parsedAmount == null || parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid positive budget amount")
            return
        }

        val categoryIdToSave = if (state.newBudgetType == BudgetType.CATEGORY) {
            state.selectedCategoryId ?: state.categories.firstOrNull()?.id
        } else null

        viewModelScope.launch {
            val result = upsertBudgetUseCase(
                profileId = profileId,
                type = state.newBudgetType,
                amount = parsedAmount,
                categoryId = categoryIdToSave,
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
