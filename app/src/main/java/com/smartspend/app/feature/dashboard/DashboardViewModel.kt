package com.smartspend.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.intelligence.StreakManager
import com.smartspend.app.domain.intelligence.StreakStatus
import com.smartspend.app.domain.model.CashFlowSummary
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.dashboard.DashboardSummary
import com.smartspend.app.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.smartspend.app.domain.usecase.export.ExportTransactionsUseCase
import com.smartspend.app.domain.usecase.income.GetCashFlowSummaryUseCase
import com.smartspend.app.domain.usecase.recurring.CalculateRecurringCommitmentsUseCase
import com.smartspend.app.domain.usecase.demo.SeedDemoDataUseCase
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
    val preferredCurrency: String = "INR",
    val todayDebits: BigDecimal = BigDecimal.ZERO,
    val carryoverSurplus: BigDecimal = BigDecimal.ZERO,
    val sevenDayAvgDebit: BigDecimal = BigDecimal.ZERO,
    val sevenDayRhythm: List<Pair<String, BigDecimal>> = emptyList(),
    val summary: DashboardSummary? = null,
    val cashFlow: CashFlowSummary? = null,
    val recurringCommitment: BigDecimal = BigDecimal.ZERO,
    val categoriesMap: Map<String, Category> = emptyMap(),
    val streakStatus: StreakStatus? = null,
    val isExportDialogOpen: Boolean = false,
    val isLoading: Boolean = true,
    val isSeedingData: Boolean = false,
    val toastMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getCashFlowSummaryUseCase: GetCashFlowSummaryUseCase,
    private val calculateRecurringCommitmentsUseCase: CalculateRecurringCommitmentsUseCase,
    val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val seedDemoDataUseCase: SeedDemoDataUseCase,
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val streakManager: StreakManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun seedDemoData(onComplete: (Int) -> Unit = {}) {
        val profileId = _uiState.value.activeProfileId
        if (profileId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSeedingData = true) }
            val result = seedDemoDataUseCase(profileId)
            result.fold(
                onSuccess = { count ->
                    _uiState.update {
                        it.copy(
                            isSeedingData = false,
                            toastMessage = "Successfully added $count demo financial records!"
                        )
                    }
                    onComplete(count)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSeedingData = false,
                            errorMessage = "Failed to load demo data: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            preferencesManager.preferredCurrencyFlow.collect { curr ->
                _uiState.update { it.copy(preferredCurrency = curr) }
            }
        }

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

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.flatMapLatest { profileId ->
                if (profileId != null) {
                    expenseRepository.getAllExpenses(profileId)
                } else {
                    flowOf(emptyList())
                }
            }.collect { expenses ->
                val streak = streakManager.calculateStreak(expenses)

                // Calculate today's debits
                val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val todayEnd = todayStart + (24 * 60 * 60 * 1000) - 1
                val todaySum = expenses
                    .filter { it.date in todayStart..todayEnd }
                    .fold(BigDecimal.ZERO) { acc, exp -> acc.add(exp.amount) }

                // Calculate 7-day rhythm and daily average
                val sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val last7DaysExpenses = expenses.filter { it.date >= sevenDaysAgo }
                val last7DaysSum = last7DaysExpenses.fold(BigDecimal.ZERO) { acc, exp -> acc.add(exp.amount) }
                val sevenDayAvg = if (last7DaysExpenses.isNotEmpty()) {
                    last7DaysSum.divide(BigDecimal("7"), 2, java.math.RoundingMode.HALF_EVEN)
                } else BigDecimal.ZERO

                val rhythm = (6 downTo 0).map { daysBack ->
                    val day = LocalDate.now().minusDays(daysBack.toLong())
                    val dStart = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dEnd = dStart + (24 * 60 * 60 * 1000) - 1
                    val dayTotal = expenses.filter { it.date in dStart..dEnd }
                        .fold(BigDecimal.ZERO) { acc, exp -> acc.add(exp.amount) }
                    val label = "${day.dayOfMonth} ${day.dayOfWeek.name.take(1)}"
                    label to dayTotal
                }

                _uiState.update {
                    it.copy(
                        streakStatus = streak,
                        todayDebits = todaySum,
                        sevenDayAvgDebit = sevenDayAvg,
                        sevenDayRhythm = rhythm
                    )
                }
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
