package com.smartspend.app.feature.assisted.ocr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.assisted.ParsedExpenseDraft
import com.smartspend.app.domain.assisted.ReceiptOcrParser
import com.smartspend.app.domain.assisted.SmartCategorySuggester
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.expense.AddExpenseUseCase
import com.smartspend.app.domain.usecase.expense.DuplicateGuardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

data class ReceiptScannerUiState(
    val isScanning: Boolean = false,
    val capturedImageUri: Uri? = null,
    val rawOcrText: String = "",
    val parsedDraft: ParsedExpenseDraft? = null,
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val availableCategories: List<Category> = emptyList(),
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val duplicateWarning: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReceiptScannerViewModel @Inject constructor(
    private val ocrParser: ReceiptOcrParser,
    private val categorySuggester: SmartCategorySuggester,
    private val duplicateGuard: DuplicateGuardUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptScannerUiState())
    val uiState: StateFlow<ReceiptScannerUiState> = _uiState.asStateFlow()

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
                availablePaymentMethods = paymentMethods,
                selectedCategory = categories.firstOrNull(),
                selectedPaymentMethod = paymentMethods.firstOrNull()
            )
        }
    }

    fun processImageUri(context: Context, uri: Uri, isScreenshot: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                capturedImageUri = uri,
                errorMessage = null
            )

            try {
                val inputImage = withContext(Dispatchers.IO) {
                    InputImage.fromFilePath(context, uri)
                }

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val visionText = recognizer.process(inputImage).await()
                val rawText = visionText.text

                if (rawText.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        errorMessage = "No readable text detected in receipt. Please try taking a clearer photo."
                    )
                    return@launch
                }

                val source = if (isScreenshot) ExpenseSource.SCREENSHOT else ExpenseSource.OCR
                val draft = ocrParser.parse(rawText, source)
                val state = _uiState.value

                // Match category
                val matchedCategory = if (draft.suggestedCategoryName != null) {
                    categorySuggester.suggestCategory(draft.title + " " + draft.suggestedCategoryName, state.availableCategories)
                        ?: state.availableCategories.find { it.name.equals(draft.suggestedCategoryName, ignoreCase = true) }
                } else {
                    categorySuggester.suggestCategory(draft.title, state.availableCategories)
                } ?: state.selectedCategory ?: state.availableCategories.firstOrNull()

                // Match payment method
                val matchedPaymentMethod = if (draft.suggestedPaymentType != null) {
                    state.availablePaymentMethods.find { it.type == draft.suggestedPaymentType }
                } else null ?: state.selectedPaymentMethod ?: state.availablePaymentMethods.firstOrNull()

                _uiState.value = state.copy(
                    isScanning = false,
                    rawOcrText = rawText,
                    parsedDraft = draft,
                    selectedCategory = matchedCategory,
                    selectedPaymentMethod = matchedPaymentMethod
                )

                // Check for duplicates
                val profileId = activeProfileId
                if (profileId != null && draft.amount != null) {
                    val duplicateResult = duplicateGuard(profileId, draft.title, draft.amount, draft.date)
                    _uiState.value = _uiState.value.copy(
                        duplicateWarning = if (duplicateResult.isDuplicate) duplicateResult.warningMessage else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = "Failed to scan receipt: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = paymentMethod)
    }

    fun onTitleChanged(title: String) {
        val draft = _uiState.value.parsedDraft ?: return
        _uiState.value = _uiState.value.copy(parsedDraft = draft.copy(title = title))
    }

    fun onAmountChanged(amountStr: String) {
        val draft = _uiState.value.parsedDraft ?: return
        try {
            val amount = BigDecimal(amountStr)
            _uiState.value = _uiState.value.copy(parsedDraft = draft.copy(amount = amount))
        } catch (_: Exception) {}
    }

    fun saveExpense() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return
        val draft = state.parsedDraft

        if (draft == null || draft.amount == null || draft.amount <= BigDecimal.ZERO) {
            _uiState.value = state.copy(errorMessage = "Please ensure the scanned expense has a valid positive amount")
            return
        }

        val category = state.selectedCategory ?: state.availableCategories.firstOrNull()
        if (category == null) {
            _uiState.value = state.copy(errorMessage = "Please select a category")
            return
        }

        val paymentMethod = state.selectedPaymentMethod ?: state.availablePaymentMethods.firstOrNull()
        if (paymentMethod == null) {
            _uiState.value = state.copy(errorMessage = "Please select a payment method")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)

            val result = addExpenseUseCase(
                profileId = profileId,
                title = draft.title,
                amount = draft.amount,
                currency = draft.currency,
                categoryId = category.id,
                paymentMethodId = paymentMethod.id,
                date = draft.date,
                notes = draft.notes,
                isRecurring = false,
                source = draft.source,
                attachmentRef = state.capturedImageUri?.toString()
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Failed to save receipt expense"
                    )
                }
            )
        }
    }

    fun resetScan() {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            capturedImageUri = null,
            rawOcrText = "",
            parsedDraft = null,
            duplicateWarning = null,
            errorMessage = null
        )
    }
}
