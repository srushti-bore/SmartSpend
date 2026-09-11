package com.smartspend.app.feature.assisted.importcsv

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.export.CsvImportUseCase
import com.smartspend.app.domain.usecase.export.CsvParseResult
import com.smartspend.app.domain.usecase.export.ImportCandidateExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CsvImportUiState(
    val isLoading: Boolean = false,
    val parseResult: CsvParseResult? = null,
    val candidates: List<ImportCandidateExpense> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val isImporting: Boolean = false,
    val importedCount: Int? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val csvImportUseCase: CsvImportUseCase,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvImportUiState())
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            activeProfileId = profileId

            val categories = categoryRepository.getCategories(profileId).firstOrNull() ?: emptyList()
            val paymentMethods = paymentMethodRepository.getPaymentMethods(profileId).firstOrNull() ?: emptyList()

            _uiState.value = _uiState.value.copy(
                availableCategories = categories,
                availablePaymentMethods = paymentMethods
            )
        }
    }

    fun parseCsvFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val profileId = activeProfileId ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, importedCount = null)

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not open selected CSV file"
                    )
                    return@launch
                }

                val categories = _uiState.value.availableCategories
                val paymentMethods = _uiState.value.availablePaymentMethods

                val result = csvImportUseCase.parseCsv(inputStream, profileId, categories, paymentMethods)

                if (result.validCandidates.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No valid expense rows found in CSV. Please verify column headers (Date, Title, Amount)."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    parseResult = result,
                    candidates = result.validCandidates
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to parse CSV: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun toggleCandidateSelection(candidateId: String) {
        val updated = _uiState.value.candidates.map {
            if (it.id == candidateId) it.copy(isSelected = !it.isSelected) else it
        }
        _uiState.value = _uiState.value.copy(candidates = updated)
    }

    fun selectAll(select: Boolean) {
        val updated = _uiState.value.candidates.map { it.copy(isSelected = select) }
        _uiState.value = _uiState.value.copy(candidates = updated)
    }

    fun updateCandidateCategory(candidateId: String, category: Category) {
        val updated = _uiState.value.candidates.map {
            if (it.id == candidateId) it.copy(suggestedCategory = category) else it
        }
        _uiState.value = _uiState.value.copy(candidates = updated)
    }

    fun commitImport() {
        val profileId = activeProfileId ?: return
        val candidates = _uiState.value.candidates

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, errorMessage = null)

            try {
                val count = csvImportUseCase.commitImport(profileId, candidates)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importedCount = count
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = "Failed to import expenses: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }
}
