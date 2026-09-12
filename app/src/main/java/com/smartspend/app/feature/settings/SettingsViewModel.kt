package com.smartspend.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.backup.EncryptedBackupUseCase
import com.smartspend.app.domain.usecase.backup.EncryptedRestoreUseCase
import com.smartspend.app.domain.usecase.backup.RestoreSummary
import com.smartspend.app.domain.usecase.export.ExportTransactionsUseCase
import com.smartspend.app.domain.usecase.profile.UpdateProfileCredentialUseCase
import com.smartspend.app.domain.usecase.profile.WipeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BarChartItem(
    val label: String,
    val amount: BigDecimal,
    val percentage: Float = 0f,
    val isPeak: Boolean = false
)

data class DonutChartSegment(
    val categoryName: String,
    val amount: BigDecimal,
    val percentage: Float,
    val colorIndex: Int
)

data class SettingsUiState(
    // Active Profile & Preferences
    val activeProfile: Profile? = null,
    val themeMode: String = "SYSTEM",
    val preferredCurrency: String = "INR",
    val activeSectionTab: String = "ALL",

    // Section 1: Display & Region
    val dateFormat: String = "YYYY-MM-DD",

    // Section 2: Budget & Alerts
    val alertThreshold80: Boolean = true,
    val alertThreshold100: Boolean = true,
    val dailyPacingAlerts: Boolean = true,
    val recurringDueAlerts: Boolean = true,

    // Section 3: Account & Security
    val isResetPinDialogOpen: Boolean = false,
    val isDeleteAccountDialogOpen: Boolean = false,
    val selectedAuthType: AuthType = AuthType.PIN,
    val newPinInput: String = "",
    val confirmPinInput: String = "",
    val patternStep: Int = 1,
    val patternCredential: String = "",
    val confirmPatternCredential: String = "",
    val isPatternError: Boolean = false,
    val patternHint: String = "Connect at least 4 dots",
    val pinErrorMessage: String? = null,
    val pinSuccessMessage: String? = null,
    val biometricEnabled: Boolean = true,

    // Section 4: Data & Backup (SRS Phase 5)
    val isExportingBackup: Boolean = false,
    val isRestoringBackup: Boolean = false,
    val backupFileCreated: File? = null,
    val restoreSummary: RestoreSummary? = null,
    val backupErrorMessage: String? = null,
    val backupSuccessMessage: String? = null,
    val lastBackupTimestamp: Long? = null,

    // Section 5: Notifications
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "09:00 PM",
    val milestoneAlertsEnabled: Boolean = true,

    // Section 6: Analytics
    val analyticsTimeframe: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY
    val analyticsBarItems: List<BarChartItem> = emptyList(),
    val analyticsDonutSegments: List<DonutChartSegment> = emptyList(),
    val analyticsTotalSpend: BigDecimal = BigDecimal.ZERO,
    val analyticsPeakSpend: BigDecimal = BigDecimal.ZERO,
    val analyticsCategories: List<CategorySpendAggregate> = emptyList(),
    val isAnalyticsLoading: Boolean = false,

    // Section 7: Transaction History
    val historySelectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val historySelectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-indexed
    val historyMonthTitle: String = "",
    val historyExpenses: List<Expense> = emptyList(),
    val historyIncomes: List<Income> = emptyList(),
    val historyTotalDebit: BigDecimal = BigDecimal.ZERO,
    val historyTotalCredit: BigDecimal = BigDecimal.ZERO,
    val isHistoryExporting: Boolean = false,
    val historyExportSuccessMessage: String? = null,

    // Metadata & Reference Maps
    val categoriesMap: Map<String, Category> = emptyMap(),
    val paymentMethodsMap: Map<String, PaymentMethod> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager,
    private val updateProfileCredentialUseCase: UpdateProfileCredentialUseCase,
    private val wipeDataUseCase: WipeDataUseCase,
    private val encryptedBackupUseCase: EncryptedBackupUseCase,
    private val encryptedRestoreUseCase: EncryptedRestoreUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        loadBaseData()
    }

    private fun loadBaseData() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        _uiState.value = _uiState.value.copy(
            historySelectedYear = currentYear,
            historySelectedMonth = currentMonth,
            historyMonthTitle = monthTitle
        )

        // Observe Preferences
        viewModelScope.launch {
            preferencesManager.themeModeFlow.collect { theme ->
                _uiState.value = _uiState.value.copy(themeMode = theme)
            }
        }

        viewModelScope.launch {
            preferencesManager.preferredCurrencyFlow.collect { currency ->
                _uiState.value = _uiState.value.copy(preferredCurrency = currency)
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    val profile = profileRepository.getProfileById(profileId)
                    _uiState.value = _uiState.value.copy(
                        activeProfile = profile,
                        selectedAuthType = profile?.primaryAuthType ?: AuthType.PIN
                    )

                    // Load categories & payment methods maps
                    loadReferenceMaps(profileId)
                    // Load analytics data
                    loadAnalyticsData(profileId, _uiState.value.analyticsTimeframe)
                    // Load transaction history data
                    loadTransactionHistory(profileId, currentYear, currentMonth)
                }
            }
        }
    }

    private fun loadReferenceMaps(profileId: String) {
        viewModelScope.launch {
            categoryRepository.getCategories(profileId).collect { cats ->
                _uiState.value = _uiState.value.copy(
                    categoriesMap = cats.associateBy { it.id }
                )
            }
        }
        viewModelScope.launch {
            paymentMethodRepository.getPaymentMethods(profileId).collect { pms ->
                _uiState.value = _uiState.value.copy(
                    paymentMethodsMap = pms.associateBy { it.id }
                )
            }
        }
    }

    // ==========================================
    // 1. DISPLAY & REGION
    // ==========================================
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun setPreferredCurrency(currency: String) {
        viewModelScope.launch {
            preferencesManager.setPreferredCurrency(currency)
            _uiState.update { it.copy(preferredCurrency = currency) }
        }
    }

    // ==========================================
    // 2. BUDGET & ALERTS
    // ==========================================
    fun toggleAlertThreshold80() {
        _uiState.value = _uiState.value.copy(alertThreshold80 = !_uiState.value.alertThreshold80)
    }

    fun toggleAlertThreshold100() {
        _uiState.value = _uiState.value.copy(alertThreshold100 = !_uiState.value.alertThreshold100)
    }

    fun toggleDailyPacingAlerts() {
        _uiState.value = _uiState.value.copy(dailyPacingAlerts = !_uiState.value.dailyPacingAlerts)
    }

    fun toggleRecurringDueAlerts() {
        _uiState.value = _uiState.value.copy(recurringDueAlerts = !_uiState.value.recurringDueAlerts)
    }

    // ==========================================
    // 3. ACCOUNT & SECURITY
    // ==========================================
    fun openResetPinDialog() {
        val currentType = _uiState.value.activeProfile?.primaryAuthType ?: AuthType.PIN
        _uiState.value = _uiState.value.copy(
            isResetPinDialogOpen = true,
            selectedAuthType = currentType,
            newPinInput = "",
            confirmPinInput = "",
            patternStep = 1,
            patternCredential = "",
            confirmPatternCredential = "",
            isPatternError = false,
            patternHint = "Connect at least 4 dots",
            pinErrorMessage = null,
            pinSuccessMessage = null
        )
    }

    fun closeResetPinDialog() {
        _uiState.value = _uiState.value.copy(
            isResetPinDialogOpen = false,
            pinErrorMessage = null
        )
    }

    fun onAuthTypeSelected(type: AuthType) {
        _uiState.value = _uiState.value.copy(
            selectedAuthType = type,
            newPinInput = "",
            confirmPinInput = "",
            patternStep = 1,
            patternCredential = "",
            confirmPatternCredential = "",
            isPatternError = false,
            patternHint = "Connect at least 4 dots",
            pinErrorMessage = null
        )
    }

    fun onNewPinChange(pin: String) {
        _uiState.value = _uiState.value.copy(newPinInput = pin, pinErrorMessage = null)
    }

    fun onConfirmPinChange(pin: String) {
        _uiState.value = _uiState.value.copy(confirmPinInput = pin, pinErrorMessage = null)
    }

    fun onPatternStarted() {
        _uiState.value = _uiState.value.copy(isPatternError = false, pinErrorMessage = null)
    }

    fun onPatternCompleted(pattern: String) {
        if (pattern.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isPatternError = true,
                pinErrorMessage = "Connect at least 4 dots"
            )
            return
        }

        val state = _uiState.value
        if (state.patternStep == 1) {
            _uiState.value = state.copy(
                patternCredential = pattern,
                patternStep = 2,
                patternHint = "Draw the pattern again to confirm",
                pinErrorMessage = null,
                isPatternError = false
            )
        } else if (state.patternStep == 2) {
            if (pattern == state.patternCredential) {
                _uiState.value = state.copy(
                    confirmPatternCredential = pattern,
                    patternStep = 3,
                    patternHint = "Pattern confirmed! Click Save below.",
                    pinErrorMessage = null,
                    isPatternError = false
                )
            } else {
                _uiState.value = state.copy(
                    isPatternError = true,
                    patternStep = 1,
                    patternCredential = "",
                    confirmPatternCredential = "",
                    patternHint = "Pattern mismatch. Draw again from step 1.",
                    pinErrorMessage = "Patterns did not match."
                )
            }
        }
    }

    fun resetPatternInput() {
        _uiState.value = _uiState.value.copy(
            patternStep = 1,
            patternCredential = "",
            confirmPatternCredential = "",
            isPatternError = false,
            patternHint = "Connect at least 4 dots",
            pinErrorMessage = null
        )
    }

    fun saveNewCredential() {
        val state = _uiState.value
        val profile = state.activeProfile ?: return

        val credentialToSave = if (state.selectedAuthType == AuthType.PATTERN) {
            if (state.patternCredential.isBlank() || state.patternCredential != state.confirmPatternCredential) {
                _uiState.value = state.copy(pinErrorMessage = "Please complete and confirm your pattern")
                return
            }
            state.patternCredential
        } else {
            if (state.newPinInput.length < 4) {
                _uiState.value = state.copy(pinErrorMessage = "${state.selectedAuthType.name} must be at least 4 characters")
                return
            }
            if (state.newPinInput != state.confirmPinInput) {
                _uiState.value = state.copy(pinErrorMessage = "${state.selectedAuthType.name}s do not match")
                return
            }
            state.newPinInput
        }

        viewModelScope.launch {
            val result = updateProfileCredentialUseCase(
                profileId = profile.id,
                newCredential = credentialToSave,
                newAuthType = state.selectedAuthType
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isResetPinDialogOpen = false,
                    pinSuccessMessage = "${state.selectedAuthType.name} updated successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    pinErrorMessage = result.exceptionOrNull()?.message ?: "Failed to update credential"
                )
            }
        }
    }

    fun openDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(isDeleteAccountDialogOpen = true)
    }

    fun closeDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(isDeleteAccountDialogOpen = false)
    }

    fun wipeAndResetAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            wipeDataUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false, isDeleteAccountDialogOpen = false)
            onComplete()
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            onComplete()
        }
    }

    // ==========================================
    // 4. DATA & BACKUP (SRS PHASE 5)
    // ==========================================
    fun exportEncryptedBackup(context: Context) {
        val profileId = activeProfileId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExportingBackup = true,
                backupErrorMessage = null,
                backupSuccessMessage = null,
                backupFileCreated = null
            )

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(context.cacheDir, "smartspend_backup_$timeStamp.smartspend")

            val result = encryptedBackupUseCase.createBackup(profileId, backupFile)
            result.fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isExportingBackup = false,
                        backupFileCreated = file,
                        lastBackupTimestamp = System.currentTimeMillis(),
                        backupSuccessMessage = "Encrypted backup file created (${file.length() / 1024} KB). Sharing..."
                    )
                    shareBackupFile(context, file)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isExportingBackup = false,
                        backupErrorMessage = error.localizedMessage ?: "Failed to export encrypted backup"
                    )
                }
            )
        }
    }

    fun restoreFromBackupFile(context: Context, uri: Uri) {
        val profileId = activeProfileId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoringBackup = true,
                backupErrorMessage = null,
                backupSuccessMessage = null,
                restoreSummary = null
            )

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _uiState.value = _uiState.value.copy(
                        isRestoringBackup = false,
                        backupErrorMessage = "Could not read backup file"
                    )
                    return@launch
                }

                val result = encryptedRestoreUseCase.restoreBackup(profileId, inputStream)
                result.fold(
                    onSuccess = { summary ->
                        _uiState.value = _uiState.value.copy(
                            isRestoringBackup = false,
                            restoreSummary = summary,
                            backupSuccessMessage = "Restored ${summary.expensesCount} expenses & ${summary.incomesCount} incomes successfully."
                        )
                        // Refresh data
                        loadAnalyticsData(profileId, _uiState.value.analyticsTimeframe)
                        loadTransactionHistory(profileId, _uiState.value.historySelectedYear, _uiState.value.historySelectedMonth)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isRestoringBackup = false,
                            backupErrorMessage = "Restore failed: ${error.localizedMessage ?: error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoringBackup = false,
                    backupErrorMessage = e.localizedMessage ?: "Failed to restore backup"
                )
            }
        }
    }

    private fun shareBackupFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save Encrypted SmartSpend Backup"))
        } catch (_: Exception) {}
    }

    // ==========================================
    // 5. NOTIFICATIONS
    // ==========================================
    fun toggleDailyReminder() {
        _uiState.value = _uiState.value.copy(dailyReminderEnabled = !_uiState.value.dailyReminderEnabled)
    }

    fun toggleMilestoneAlerts() {
        _uiState.value = _uiState.value.copy(milestoneAlertsEnabled = !_uiState.value.milestoneAlertsEnabled)
    }

    // ==========================================
    // 6. ANALYTICS (DAILY, WEEKLY, MONTHLY)
    // ==========================================
    fun setAnalyticsTimeframe(timeframe: String) {
        _uiState.value = _uiState.value.copy(analyticsTimeframe = timeframe)
        val profileId = activeProfileId ?: return
        loadAnalyticsData(profileId, timeframe)
    }

    private fun loadAnalyticsData(profileId: String, timeframe: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyticsLoading = true)

            when (timeframe) {
                "DAILY" -> calculateDailyAnalytics(profileId)
                "WEEKLY" -> calculateWeeklyAnalytics(profileId)
                "MONTHLY" -> calculateMonthlyAnalytics(profileId)
            }
        }
    }

    private suspend fun calculateDailyAnalytics(profileId: String) {
        // Last 7 days
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endMs = cal.timeInMillis

        val startCal = Calendar.getInstance()
        startCal.add(Calendar.DAY_OF_YEAR, -6)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        val startMs = startCal.timeInMillis

        val expenses = expenseRepository.getExpensesBetweenDates(profileId, startMs, endMs).firstOrNull() ?: emptyList()
        val categories = expenseRepository.getCategorySpending(profileId, startMs, endMs).firstOrNull() ?: emptyList()

        val dayFormat = SimpleDateFormat("EEE\ndd", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val dayMap = mutableMapOf<String, Pair<String, BigDecimal>>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = dayKeyFormat.format(c.time)
            val label = dayFormat.format(c.time)
            dayMap[key] = Pair(label, BigDecimal.ZERO)
        }

        for (exp in expenses) {
            val key = dayKeyFormat.format(Date(exp.date))
            dayMap[key]?.let { (label, currentSum) ->
                dayMap[key] = Pair(label, currentSum.add(exp.amount))
            }
        }

        val barList = dayMap.values.map { (label, amount) ->
            BarChartItem(label = label, amount = amount)
        }

        finishAnalyticsCalculation(barList, categories)
    }

    private suspend fun calculateWeeklyAnalytics(profileId: String) {
        // 4 Weeks of Current Month
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startMs = cal.timeInMillis

        val endCal = Calendar.getInstance()
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        val endMs = endCal.timeInMillis

        val expenses = expenseRepository.getExpensesBetweenDates(profileId, startMs, endMs).firstOrNull() ?: emptyList()
        val categories = expenseRepository.getCategorySpending(profileId, startMs, endMs).firstOrNull() ?: emptyList()

        val weekSums = Array(4) { BigDecimal.ZERO }
        for (exp in expenses) {
            val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
            val day = expCal.get(Calendar.DAY_OF_MONTH)
            val weekIdx = when {
                day <= 7 -> 0
                day <= 14 -> 1
                day <= 21 -> 2
                else -> 3
            }
            weekSums[weekIdx] = weekSums[weekIdx].add(exp.amount)
        }

        val barList = listOf(
            BarChartItem(label = "W1\n1-7", amount = weekSums[0]),
            BarChartItem(label = "W2\n8-14", amount = weekSums[1]),
            BarChartItem(label = "W3\n15-21", amount = weekSums[2]),
            BarChartItem(label = "W4\n22+", amount = weekSums[3])
        )

        finishAnalyticsCalculation(barList, categories)
    }

    private suspend fun calculateMonthlyAnalytics(profileId: String) {
        // Last 6 Months
        val monthFormat = SimpleDateFormat("MMM\nyy", Locale.getDefault())
        val monthKeyFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endMs = cal.timeInMillis

        val startCal = Calendar.getInstance()
        startCal.add(Calendar.MONTH, -5)
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        val startMs = startCal.timeInMillis

        val expenses = expenseRepository.getExpensesBetweenDates(profileId, startMs, endMs).firstOrNull() ?: emptyList()
        val categories = expenseRepository.getCategorySpending(profileId, startMs, endMs).firstOrNull() ?: emptyList()

        val monthMap = mutableMapOf<String, Pair<String, BigDecimal>>()
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val key = monthKeyFormat.format(c.time)
            val label = monthFormat.format(c.time)
            monthMap[key] = Pair(label, BigDecimal.ZERO)
        }

        for (exp in expenses) {
            val key = monthKeyFormat.format(Date(exp.date))
            monthMap[key]?.let { (label, sum) ->
                monthMap[key] = Pair(label, sum.add(exp.amount))
            }
        }

        val barList = monthMap.values.map { (label, amount) ->
            BarChartItem(label = label, amount = amount)
        }

        finishAnalyticsCalculation(barList, categories)
    }

    private fun finishAnalyticsCalculation(barList: List<BarChartItem>, categories: List<CategorySpendAggregate>) {
        val totalSpend = barList.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
        val maxAmount = barList.maxOfOrNull { it.amount } ?: BigDecimal.ZERO

        val normalizedBars = barList.map { item ->
            val pct = if (maxAmount > BigDecimal.ZERO) {
                item.amount.divide(maxAmount, 4, RoundingMode.HALF_EVEN).toFloat()
            } else 0f
            item.copy(percentage = pct, isPeak = (item.amount == maxAmount && maxAmount > BigDecimal.ZERO))
        }

        // Donut segments
        val totalCatAmount = categories.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.totalAmount) }
        val donutSegments = categories.mapIndexed { idx, cat ->
            val pct = if (totalCatAmount > BigDecimal.ZERO) {
                cat.totalAmount.multiply(BigDecimal(100)).divide(totalCatAmount, 1, RoundingMode.HALF_EVEN).toFloat()
            } else 0f
            DonutChartSegment(
                categoryName = cat.categoryName,
                amount = cat.totalAmount,
                percentage = pct,
                colorIndex = idx % 8
            )
        }

        _uiState.value = _uiState.value.copy(
            isAnalyticsLoading = false,
            analyticsBarItems = normalizedBars,
            analyticsDonutSegments = donutSegments,
            analyticsTotalSpend = totalSpend,
            analyticsPeakSpend = maxAmount,
            analyticsCategories = categories
        )
    }

    // ==========================================
    // 7. TRANSACTION HISTORY (MONTH-WISE & EXPORT)
    // ==========================================
    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _uiState.value.historySelectedYear)
            set(Calendar.MONTH, _uiState.value.historySelectedMonth)
            add(Calendar.MONTH, -1)
        }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val title = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        _uiState.value = _uiState.value.copy(
            historySelectedYear = y,
            historySelectedMonth = m,
            historyMonthTitle = title
        )
        activeProfileId?.let { loadTransactionHistory(it, y, m) }
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _uiState.value.historySelectedYear)
            set(Calendar.MONTH, _uiState.value.historySelectedMonth)
            add(Calendar.MONTH, 1)
        }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val title = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        _uiState.value = _uiState.value.copy(
            historySelectedYear = y,
            historySelectedMonth = m,
            historyMonthTitle = title
        )
        activeProfileId?.let { loadTransactionHistory(it, y, m) }
    }

    private fun loadTransactionHistory(profileId: String, year: Int, month: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startMs = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endMs = cal.timeInMillis

            val expenses = expenseRepository.getExpensesBetweenDates(profileId, startMs, endMs).firstOrNull() ?: emptyList()
            val incomes = incomeRepository.getIncomesBetweenDates(profileId, startMs, endMs).firstOrNull() ?: emptyList()

            val totalDebit = expenses.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }
            val totalCredit = incomes.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.amount) }

            _uiState.value = _uiState.value.copy(
                historyExpenses = expenses.sortedByDescending { it.date },
                historyIncomes = incomes.sortedByDescending { it.date },
                historyTotalDebit = totalDebit,
                historyTotalCredit = totalCredit,
                historyExportSuccessMessage = null
            )
        }
    }

    fun exportMonthCsv(context: Context) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHistoryExporting = true)

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, _uiState.value.historySelectedYear)
                set(Calendar.MONTH, _uiState.value.historySelectedMonth)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startMs = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endMs = cal.timeInMillis

            val csvContent = exportTransactionsUseCase(profileId, startMs, endMs)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                type = "text/csv"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export ${_uiState.value.historyMonthTitle} CSV"))

            _uiState.value = _uiState.value.copy(
                isHistoryExporting = false,
                historyExportSuccessMessage = "CSV exported successfully"
            )
        }
    }

    fun exportMonthPdf(context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHistoryExporting = true)

            val result = PdfExporter.exportMonthlyStatement(
                context = context,
                profile = state.activeProfile,
                monthTitle = state.historyMonthTitle,
                expenses = state.historyExpenses,
                incomes = state.historyIncomes,
                categoriesMap = state.categoriesMap,
                paymentMethodsMap = state.paymentMethodsMap,
                currency = state.preferredCurrency
            )

            result.fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isHistoryExporting = false,
                        historyExportSuccessMessage = "PDF statement generated (${file.length() / 1024} KB)"
                    )
                    PdfExporter.sharePdfFile(context, file, "SmartSpend Statement - ${state.historyMonthTitle}")
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isHistoryExporting = false,
                        backupErrorMessage = "PDF Export failed: ${err.message}"
                    )
                }
            )
        }
    }
}
