package com.smartspend.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.usecase.profile.CreateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val name: String = "",
    val authType: AuthType = AuthType.PIN,
    val credential: String = "",
    val confirmCredential: String = "",
    val patternStep: Int = 1, // 1: draw pattern, 2: confirm pattern, 3: pattern confirmed
    val isPatternError: Boolean = false,
    val patternHint: String = "Connect at least 4 dots",
    val biometricEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun onAuthTypeChange(authType: AuthType) {
        _uiState.value = _uiState.value.copy(
            authType = authType,
            credential = "",
            confirmCredential = "",
            patternStep = 1,
            isPatternError = false,
            patternHint = "Connect at least 4 dots",
            errorMessage = null
        )
    }

    fun onCredentialChange(credential: String) {
        _uiState.value = _uiState.value.copy(credential = credential, errorMessage = null)
    }

    fun onConfirmCredentialChange(confirm: String) {
        _uiState.value = _uiState.value.copy(confirmCredential = confirm, errorMessage = null)
    }

    fun onPatternStarted() {
        _uiState.value = _uiState.value.copy(isPatternError = false, errorMessage = null)
    }

    fun onPatternCompleted(pattern: String) {
        if (pattern.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isPatternError = true,
                errorMessage = "Connect at least 4 dots to form a pattern"
            )
            return
        }

        val state = _uiState.value
        if (state.patternStep == 1) {
            // First pattern recorded
            _uiState.value = state.copy(
                credential = pattern,
                patternStep = 2,
                patternHint = "Draw the pattern again to confirm",
                errorMessage = null,
                isPatternError = false
            )
        } else if (state.patternStep == 2) {
            // Confirmation pattern
            if (pattern == state.credential) {
                _uiState.value = state.copy(
                    confirmCredential = pattern,
                    patternStep = 3,
                    patternHint = "Pattern confirmed successfully!",
                    errorMessage = null,
                    isPatternError = false
                )
            } else {
                _uiState.value = state.copy(
                    isPatternError = true,
                    patternStep = 1,
                    credential = "",
                    confirmCredential = "",
                    patternHint = "Patterns did not match. Please draw again.",
                    errorMessage = "Pattern mismatch. Try drawing again."
                )
            }
        }
    }

    fun resetPattern() {
        _uiState.value = _uiState.value.copy(
            credential = "",
            confirmCredential = "",
            patternStep = 1,
            isPatternError = false,
            patternHint = "Connect at least 4 dots",
            errorMessage = null
        )
    }

    fun onBiometricToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter your name or profile title")
            return
        }
        if (state.credential.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please set your ${state.authType.name}")
            return
        }
        if (state.credential != state.confirmCredential) {
            _uiState.value = state.copy(errorMessage = "Credentials do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = createProfileUseCase(
                name = state.name,
                authType = state.authType,
                rawCredential = state.credential,
                biometricEnabled = state.biometricEnabled
            )

            result.fold(
                onSuccess = { profile ->
                    preferencesManager.setActiveProfileId(profile.id)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create profile"
                    )
                }
            )
        }
    }
}
