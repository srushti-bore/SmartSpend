package com.smartspend.app.feature.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.common.Resource
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.usecase.income.AddIncomeUseCase
import com.smartspend.app.domain.usecase.income.DeleteIncomeUseCase
import com.smartspend.app.domain.usecase.income.GetCashFlowSummaryUseCase
import com.smartspend.app.domain.usecase.income.GetIncomesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val getIncomesUseCase: GetIncomesUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val deleteIncomeUseCase: DeleteIncomeUseCase,
    private val getCashFlowSummaryUseCase: GetCashFlowSummaryUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    observeIncomes(profileId)
                    observeCashFlow(profileId)
                }
            }
        }
    }

    private fun observeIncomes(profileId: String) {
        viewModelScope.launch {
            getIncomesUseCase(profileId)
                .catch { e -> _uiState.update { it.copy(incomeResource = Resource.Error(e.message ?: "Failed to load incomes")) } }
                .collect { incomes ->
                    _uiState.update {
                        it.copy(
                            incomeResource = if (incomes.isEmpty()) Resource.Empty else Resource.Success(incomes)
                        )
                    }
                }
        }
    }

    private fun observeCashFlow(profileId: String) {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            getCashFlowSummaryUseCase(profileId, startOfMonth, endOfMonth)
                .catch { e -> _uiState.update { it.copy(cashFlowResource = Resource.Error(e.message ?: "Failed to calculate cash flow")) } }
                .collect { summary ->
                    _uiState.update { it.copy(cashFlowResource = Resource.Success(summary)) }
                }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddIncomeDialogOpen = true, userErrorMessage = null) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddIncomeDialogOpen = false, userErrorMessage = null) }
    }

    fun addIncome(
        title: String,
        amountStr: String,
        source: IncomeSource,
        dateMs: Long,
        paymentMethodId: String? = null,
        notes: String? = null,
        isRecurring: Boolean = false
    ) {
        val profileId = activeProfileId ?: return
        val amount = try {
            BigDecimal(amountStr.trim())
        } catch (e: Exception) {
            _uiState.update { it.copy(userErrorMessage = "Please enter a valid numeric amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, userErrorMessage = null) }
            val result = addIncomeUseCase(
                profileId = profileId,
                title = title,
                amount = amount,
                source = source,
                date = dateMs,
                paymentMethodId = paymentMethodId,
                notes = notes,
                isRecurring = isRecurring
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, isAddIncomeDialogOpen = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, userErrorMessage = error.message) }
                }
            )
        }
    }

    fun deleteIncome(incomeId: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            deleteIncomeUseCase(profileId, incomeId)
        }
    }
}
