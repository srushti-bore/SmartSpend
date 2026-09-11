package com.smartspend.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.dashboard.DashboardSummary
import com.smartspend.app.domain.usecase.dashboard.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val profileName: String = "",
    val summary: DashboardSummary? = null,
    val categoriesMap: Map<String, Category> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
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
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    val profile = profileRepository.getProfileById(profileId)
                    _uiState.value = _uiState.value.copy(profileName = profile?.name ?: "")

                    // Load categories map
                    categoryRepository.getCategories(profileId).collect { categories ->
                        val catMap = categories.associateBy { it.id }
                        _uiState.value = _uiState.value.copy(categoriesMap = catMap)
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
                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    isLoading = false
                )
            }
        }
    }
}
