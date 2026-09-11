package com.smartspend.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.profile.AuthenticateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val activeProfile: Profile? = null,
    val credentialInput: String = "",
    val errorMessage: String? = null,
    val isPatternError: Boolean = false,
    val isUnlocked: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authenticateProfileUseCase: AuthenticateProfileUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val activeId = preferencesManager.activeProfileIdFlow.firstOrNull()
            if (activeId != null) {
                val profile = profileRepository.getProfileById(activeId)
                _uiState.value = _uiState.value.copy(activeProfile = profile, isLoading = false)
            } else {
                val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
                if (profiles.isNotEmpty()) {
                    val first = profiles.first()
                    preferencesManager.setActiveProfileId(first.id)
                    _uiState.value = _uiState.value.copy(activeProfile = first, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onCredentialChange(input: String) {
        _uiState.value = _uiState.value.copy(credentialInput = input, errorMessage = null, isPatternError = false)
    }

    fun onPatternStarted() {
        _uiState.value = _uiState.value.copy(errorMessage = null, isPatternError = false)
    }

    fun onPatternCompleted(pattern: String) {
        if (pattern.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Connect at least 4 dots to unlock",
                isPatternError = true
            )
            return
        }

        val profile = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            val success = authenticateProfileUseCase(profile.id, pattern)
            if (success) {
                _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null, isPatternError = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Incorrect pattern. Try again.",
                    isPatternError = true
                )
            }
        }
    }

    fun unlock() {
        val profile = _uiState.value.activeProfile ?: return
        val input = _uiState.value.credentialInput

        viewModelScope.launch {
            val success = authenticateProfileUseCase(profile.id, input)
            if (success) {
                _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Incorrect ${profile.primaryAuthType.name}. Try again."
                )
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null)
    }
}
