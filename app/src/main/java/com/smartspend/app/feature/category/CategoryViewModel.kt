package com.smartspend.app.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.usecase.category.AddCategoryUseCase
import com.smartspend.app.domain.usecase.category.DeleteCategoryUseCase
import com.smartspend.app.domain.usecase.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val newCategoryName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        observeCategories()
    }

    private fun observeCategories() {
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
                    isLoading = false
                )
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = true, newCategoryName = "", errorMessage = null)
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = false, errorMessage = null)
    }

    fun onCategoryNameChange(name: String) {
        _uiState.value = _uiState.value.copy(newCategoryName = name, errorMessage = null)
    }

    fun addCategory() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return

        viewModelScope.launch {
            val result = addCategoryUseCase(profileId, state.newCategoryName)
            result.fold(
                onSuccess = {
                    closeAddDialog()
                },
                onFailure = { error ->
                    _uiState.value = state.copy(errorMessage = error.message ?: "Failed to add category")
                }
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            val result = deleteCategoryUseCase(profileId, categoryId)
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Cannot delete category"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
