package com.smartspend.app.feature.intelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.intelligence.FinancialHealthReport
import com.smartspend.app.domain.intelligence.FinancialHealthScoreUseCase
import com.smartspend.app.domain.intelligence.ForecastReport
import com.smartspend.app.domain.intelligence.LeakHunterReport
import com.smartspend.app.domain.intelligence.LeakHunterUseCase
import com.smartspend.app.domain.intelligence.PurchaseSimulationResult
import com.smartspend.app.domain.intelligence.PurchaseSimulatorUseCase
import com.smartspend.app.domain.intelligence.SafeSpendTier
import com.smartspend.app.domain.intelligence.SafeToSpendEngine
import com.smartspend.app.domain.intelligence.SafeToSpendResult
import com.smartspend.app.domain.intelligence.SpendForecasterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class IntelligenceHubUiState(
    val isLoading: Boolean = true,
    val safeToSpend: SafeToSpendResult? = null,
    val healthReport: FinancialHealthReport? = null,
    val leakReport: LeakHunterReport? = null,
    val forecastReport: ForecastReport? = null,
    val simulationResult: PurchaseSimulationResult? = null,
    val isSimulating: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class IntelligenceHubViewModel @Inject constructor(
    private val safeToSpendEngine: SafeToSpendEngine,
    private val healthScoreUseCase: FinancialHealthScoreUseCase,
    private val leakHunterUseCase: LeakHunterUseCase,
    private val purchaseSimulator: PurchaseSimulatorUseCase,
    private val spendForecaster: SpendForecasterUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntelligenceHubUiState())
    val uiState: StateFlow<IntelligenceHubUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            activeProfileId = profileId
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val safe = safeToSpendEngine.calculate(profileId)
                val health = healthScoreUseCase(profileId)
                val leaks = leakHunterUseCase(profileId)
                val forecast = spendForecaster(profileId)

                _uiState.value = IntelligenceHubUiState(
                    isLoading = false,
                    safeToSpend = safe,
                    healthReport = health,
                    leakReport = leaks,
                    forecastReport = forecast
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to generate financial intelligence"
                )
            }
        }
    }

    fun simulatePurchase(itemTitle: String, amountStr: String) {
        val profileId = activeProfileId ?: return
        try {
            val amount = BigDecimal(amountStr.trim().replace(",", ""))
            if (amount <= BigDecimal.ZERO) return

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSimulating = true)
                val result = purchaseSimulator(profileId, itemTitle, amount)
                _uiState.value = _uiState.value.copy(
                    isSimulating = false,
                    simulationResult = result
                )
            }
        } catch (_: Exception) {}
    }

    fun clearSimulation() {
        _uiState.value = _uiState.value.copy(simulationResult = null)
    }
}
