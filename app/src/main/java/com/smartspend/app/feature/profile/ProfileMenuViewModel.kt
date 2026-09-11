package com.smartspend.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.profile.UpdateProfileCredentialUseCase
import com.smartspend.app.domain.usecase.profile.WipeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileMenuUiState(
    val activeProfile: Profile? = null,
    val themeMode: String = "SYSTEM",
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
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileMenuViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager,
    private val updateProfileCredentialUseCase: UpdateProfileCredentialUseCase,
    private val wipeDataUseCase: WipeDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileMenuUiState())
    val uiState: StateFlow<ProfileMenuUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            preferencesManager.themeModeFlow.collect { theme ->
                _uiState.value = _uiState.value.copy(themeMode = theme)
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    val profile = profileRepository.getProfileById(profileId)
                    _uiState.value = _uiState.value.copy(
                        activeProfile = profile,
                        selectedAuthType = profile?.primaryAuthType ?: AuthType.PIN
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

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
}
