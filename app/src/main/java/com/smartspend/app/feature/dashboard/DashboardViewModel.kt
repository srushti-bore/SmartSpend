package com.smartspend.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.CashFlowSummary
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.dashboard.DashboardSummary
import com.smartspend.app.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.smartspend.app.domain.usecase.export.ExportTransactionsUseCase
import com.smartspend.app.domain.usecase.income.GetCashFlowSummaryUseCase
import com.smartspend.app.domain.usecase.recurring.CalculateRecurringCommitmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val activeProfileId: String = "",
    val profileName: String = "",
    val summary: DashboardSummary? = null,
    val cashFlow: CashFlowSummary? = null,
    val recurringCommitment: BigDecimal = BigDecimal.ZERO,
    val categoriesMap: Map<String, Category> = emptyMap(),
    val isExportDialogOpen: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getCashFlowSummaryUseCase: GetCashFlowSummaryUseCase,
    private val calculateRecurringCommitmentsUseCase: CalculateRecurringCommitmentsUseCase,
    val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    val profile = profileRepository.getProfileById(profileId)
                    _uiState.update {
                        it.copy(
                            activeProfileId = profileId,
                            profileName = profile?.name ?: ""
                        )
                    }

                    // Load categories map
                    categoryRepository.getCategories(profileId).collect { categories ->
                        val catMap = categories.associateBy { it.id }
                        _uiState.update { it.copy(categoriesMap = catMap) }
                    }
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    getDashboardSummaryUseCase(profileId)
                } else {
                    flowOf(null)
                }
            }.collect { summary ->
                _uiState.update {
                    it.copy(
                        summary = summary,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    getCashFlowSummaryUseCase(profileId, startOfMonth, endOfMonth)
                } else {
                    flowOf(null)
                }
            }.collect { cashFlow ->
                _uiState.update { it.copy(cashFlow = cashFlow) }
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    calculateRecurringCommitmentsUseCase(profileId)
                } else {
                    flowOf(BigDecimal.ZERO)
                }
            }.collect { commitment ->
                _uiState.update { it.copy(recurringCommitment = commitment) }
            }
        }
    }

    fun openExportDialog() {
        _uiState.update { it.copy(isExportDialogOpen = true) }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(isExportDialogOpen = false) }
    }
}
