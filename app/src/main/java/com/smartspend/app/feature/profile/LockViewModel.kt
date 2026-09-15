package com.smartspend.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.profile.AuthenticateProfileUseCase
import com.smartspend.app.domain.usecase.profile.CreateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val allProfiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val credentialInput: String = "",
    val errorMessage: String? = null,
    val isPatternError: Boolean = false,
    val isUnlocked: Boolean = false,
    val isLoading: Boolean = true,
    // Add Profile Modal State
    val isAddUserDialogOpen: Boolean = false,
    val newUserName: String = "",
    val newUserAuthType: AuthType = AuthType.PIN,
    val newUserCredential: String = "",
    val newUserConfirmCredential: String = "",
    val newUserBiometric: Boolean = true,
    val newUserErrorMessage: String? = null,
    val isCreatingUser: Boolean = false
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authenticateProfileUseCase: AuthenticateProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        observeProfiles()
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                val activeId = preferencesManager.activeProfileIdFlow.firstOrNull()
                val currentActive = profiles.firstOrNull { it.id == activeId } ?: profiles.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    allProfiles = profiles,
                    activeProfile = currentActive,
                    isLoading = false
                )
            }
        }
    }

    fun selectProfile(profile: Profile) {
        _uiState.value = _uiState.value.copy(
            activeProfile = profile,
            credentialInput = "",
            errorMessage = null,
            isPatternError = false
        )
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
                preferencesManager.setActiveProfileId(profile.id)
                _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null, isPatternError = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Incorrect pattern. Try again.",
                    isPatternError = true
                )
            }
        }
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value.credentialInput
        if (current.length < 6) {
            val updated = current + digit
            _uiState.value = _uiState.value.copy(credentialInput = updated, errorMessage = null)
            
            val profile = _uiState.value.activeProfile
            if (profile != null && (updated.length == 4 || updated.length == 6)) {
                viewModelScope.launch {
                    val success = authenticateProfileUseCase(profile.id, updated)
                    if (success) {
                        preferencesManager.setActiveProfileId(profile.id)
                        _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null)
                    }
                }
            }
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.credentialInput
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(credentialInput = current.dropLast(1), errorMessage = null)
        }
    }

    fun onPinClear() {
        _uiState.value = _uiState.value.copy(credentialInput = "", errorMessage = null)
    }

    fun unlock() {
        val profile = _uiState.value.activeProfile ?: return
        val input = _uiState.value.credentialInput

        viewModelScope.launch {
            val success = authenticateProfileUseCase(profile.id, input)
            if (success) {
                preferencesManager.setActiveProfileId(profile.id)
                _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Incorrect ${profile.primaryAuthType.name}. Try again.",
                    credentialInput = ""
                )
            }
        }
    }

    fun onBiometricSuccess() {
        val profile = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            preferencesManager.setActiveProfileId(profile.id)
            _uiState.value = _uiState.value.copy(isUnlocked = true, errorMessage = null)
        }
    }

    // Add Profile / User Dialog Handlers
    fun openAddUserDialog() {
        _uiState.value = _uiState.value.copy(
            isAddUserDialogOpen = true,
            newUserName = "",
            newUserAuthType = AuthType.PIN,
            newUserCredential = "",
            newUserConfirmCredential = "",
            newUserBiometric = true,
            newUserErrorMessage = null,
            isCreatingUser = false
        )
    }

    fun closeAddUserDialog() {
        _uiState.value = _uiState.value.copy(isAddUserDialogOpen = false, newUserErrorMessage = null)
    }

    fun onNewUserNameChange(name: String) {
        _uiState.value = _uiState.value.copy(newUserName = name, newUserErrorMessage = null)
    }

    fun onNewUserAuthTypeSelected(type: AuthType) {
        _uiState.value = _uiState.value.copy(
            newUserAuthType = type,
            newUserCredential = "",
            newUserConfirmCredential = "",
            newUserErrorMessage = null
        )
    }

    fun onNewUserCredentialChange(cred: String) {
        _uiState.value = _uiState.value.copy(newUserCredential = cred, newUserErrorMessage = null)
    }

    fun onNewUserConfirmCredentialChange(confirm: String) {
        _uiState.value = _uiState.value.copy(newUserConfirmCredential = confirm, newUserErrorMessage = null)
    }

    fun onNewUserBiometricToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(newUserBiometric = enabled)
    }

    fun createNewUser() {
        val state = _uiState.value
        val name = state.newUserName.trim()
        val cred = state.newUserCredential.trim()
        val confirm = state.newUserConfirmCredential.trim()

        if (name.isEmpty()) {
            _uiState.value = state.copy(newUserErrorMessage = "Please enter user / profile name")
            return
        }
        if (cred.isEmpty()) {
            _uiState.value = state.copy(newUserErrorMessage = "Please enter a credential")
            return
        }
        if (cred.length < 4) {
            _uiState.value = state.copy(newUserErrorMessage = "${state.newUserAuthType.name} must be at least 4 characters")
            return
        }
        if (cred != confirm) {
            _uiState.value = state.copy(newUserErrorMessage = "Credentials do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isCreatingUser = true, newUserErrorMessage = null)
            val result = createProfileUseCase(
                name = name,
                authType = state.newUserAuthType,
                rawCredential = cred,
                biometricEnabled = state.newUserBiometric
            )

            result.fold(
                onSuccess = { newProfile ->
                    preferencesManager.setActiveProfileId(newProfile.id)
                    _uiState.value = _uiState.value.copy(
                        isAddUserDialogOpen = false,
                        isCreatingUser = false,
                        activeProfile = newProfile,
                        credentialInput = "",
                        errorMessage = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingUser = false,
                        newUserErrorMessage = err.localizedMessage ?: "Failed to create profile"
                    )
                }
            )
        }
    }
}
