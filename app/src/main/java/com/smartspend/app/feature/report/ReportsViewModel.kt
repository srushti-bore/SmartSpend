package com.smartspend.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.domain.model.CashFlowSummary
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject

data class PaymentDistribution(
    val label: String,
    val totalAmount: BigDecimal,
    val percentage: Double
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val selectedTimeRange: String = "THIS_MONTH", // THIS_MONTH, LAST_MONTH, LAST_3_MONTHS
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val netSavings: BigDecimal = BigDecimal.ZERO,
    val savingsRatePct: Double = 0.0,
    val categoryBreakdown: List<CategorySpendAggregate> = emptyList(),
    val paymentDistribution: List<PaymentDistribution> = emptyList(),
    val currency: String = "INR"
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        loadData("THIS_MONTH")
    }

    fun setTimeRange(timeRange: String) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        loadData(timeRange)
    }

    private fun loadData(timeRange: String) {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            activeProfileId = profileId
            val currency = preferencesManager.preferredCurrencyFlow.firstOrNull() ?: "INR"

            _uiState.value = _uiState.value.copy(isLoading = true)

            val (startDate, endDate) = calculateDateRange(timeRange)

            val totalSpent = expenseRepository.getTotalSpending(profileId, startDate, endDate).firstOrNull() ?: BigDecimal.ZERO
            val totalInc = incomeRepository.getTotalIncome(profileId, startDate, endDate).firstOrNull() ?: BigDecimal.ZERO
            val categories = expenseRepository.getCategorySpending(profileId, startDate, endDate).firstOrNull() ?: emptyList()
            val allExpenses = expenseRepository.getExpensesBetweenDates(profileId, startDate, endDate).firstOrNull() ?: emptyList()
            val paymentMethods = paymentMethodRepository.getPaymentMethods(profileId).firstOrNull() ?: emptyList()

            val net = totalInc.subtract(totalSpent)
            val savingsPct = if (totalInc > BigDecimal.ZERO) {
                net.multiply(BigDecimal("100")).divide(totalInc, 1, RoundingMode.HALF_EVEN).toDouble()
            } else 0.0

            // Payment distribution
            val pmMap = paymentMethods.associateBy { it.id }
            val pmDistribution = allExpenses.groupBy { it.paymentMethodId }.map { (pmId, exps) ->
                val sum = exps.map { it.amount }.fold(BigDecimal.ZERO) { acc, a -> acc.add(a) }
                val pct = if (totalSpent > BigDecimal.ZERO) {
                    sum.multiply(BigDecimal("100")).divide(totalSpent, 1, RoundingMode.HALF_EVEN).toDouble()
                } else 0.0
                PaymentDistribution(
                    label = pmMap[pmId]?.label ?: "Cash",
                    totalAmount = sum,
                    percentage = pct
                )
            }.sortedByDescending { it.totalAmount }

            _uiState.value = ReportsUiState(
                isLoading = false,
                selectedTimeRange = timeRange,
                totalIncome = totalInc,
                totalExpense = totalSpent,
                netSavings = net,
                savingsRatePct = savingsPct,
                categoryBreakdown = categories,
                paymentDistribution = pmDistribution,
                currency = currency
            )
        }
    }

    private fun calculateDateRange(timeRange: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        return when (timeRange) {
            "LAST_MONTH" -> {
                val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            "LAST_3_MONTHS" -> {
                val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                Pair(start, end)
            }
            else -> {
                // THIS_MONTH
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
        }
    }
}
