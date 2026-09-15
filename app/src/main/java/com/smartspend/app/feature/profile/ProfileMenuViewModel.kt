package com.smartspend.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.profile.CreateProfileUseCase
import com.smartspend.app.domain.usecase.profile.DeleteProfileUseCase
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
    val allProfiles: List<Profile> = emptyList(),
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
    // Add Profile Dialog
    val isAddProfileDialogOpen: Boolean = false,
    val newProfileName: String = "",
    val newProfileAuthType: AuthType = AuthType.PIN,
    val newProfileCredential: String = "",
    val newProfileConfirmCredential: String = "",
    val newProfileBiometric: Boolean = true,
    val newProfileErrorMessage: String? = null,
    val isCreatingProfile: Boolean = false,
    // Delete Single Profile Confirmation
    val profileToDelete: Profile? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileMenuViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager,
    private val updateProfileCredentialUseCase: UpdateProfileCredentialUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
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
            profileRepository.getAllProfiles().collect { profiles ->
                _uiState.value = _uiState.value.copy(allProfiles = profiles)
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

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            preferencesManager.setActiveProfileId(profileId)
            val profile = profileRepository.getProfileById(profileId)
            _uiState.value = _uiState.value.copy(
                activeProfile = profile,
                selectedAuthType = profile?.primaryAuthType ?: AuthType.PIN,
                pinSuccessMessage = "Switched to ${profile?.name ?: "Profile"}"
            )
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    // Passcode / Pin Reset Dialog Handlers
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

    // Add Profile Handlers
    fun openAddProfileDialog() {
        _uiState.value = _uiState.value.copy(
            isAddProfileDialogOpen = true,
            newProfileName = "",
            newProfileAuthType = AuthType.PIN,
            newProfileCredential = "",
            newProfileConfirmCredential = "",
            newProfileBiometric = true,
            newProfileErrorMessage = null,
            isCreatingProfile = false
        )
    }

    fun closeAddProfileDialog() {
        _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = false, newProfileErrorMessage = null)
    }

    fun onNewProfileNameChange(name: String) {
        _uiState.value = _uiState.value.copy(newProfileName = name, newProfileErrorMessage = null)
    }

    fun onNewProfileAuthTypeSelected(type: AuthType) {
        _uiState.value = _uiState.value.copy(
            newProfileAuthType = type,
            newProfileCredential = "",
            newProfileConfirmCredential = "",
            newProfileErrorMessage = null
        )
    }

    fun onNewProfileCredentialChange(cred: String) {
        _uiState.value = _uiState.value.copy(newProfileCredential = cred, newProfileErrorMessage = null)
    }

    fun onNewProfileConfirmCredentialChange(confirm: String) {
        _uiState.value = _uiState.value.copy(newProfileConfirmCredential = confirm, newProfileErrorMessage = null)
    }

    fun onNewProfileBiometricToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(newProfileBiometric = enabled)
    }

    fun createNewProfile() {
        val state = _uiState.value
        val name = state.newProfileName.trim()
        val cred = state.newProfileCredential.trim()
        val confirm = state.newProfileConfirmCredential.trim()

        if (name.isEmpty()) {
            _uiState.value = state.copy(newProfileErrorMessage = "Please enter profile name")
            return
        }
        if (cred.isEmpty()) {
            _uiState.value = state.copy(newProfileErrorMessage = "Please set a credential")
            return
        }
        if (cred.length < 4) {
            _uiState.value = state.copy(newProfileErrorMessage = "${state.newProfileAuthType.name} must be at least 4 characters")
            return
        }
        if (cred != confirm) {
            _uiState.value = state.copy(newProfileErrorMessage = "Credentials do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isCreatingProfile = true, newProfileErrorMessage = null)
            val result = createProfileUseCase(
                name = name,
                authType = state.newProfileAuthType,
                rawCredential = cred,
                biometricEnabled = state.newProfileBiometric
            )

            result.fold(
                onSuccess = { created ->
                    preferencesManager.setActiveProfileId(created.id)
                    _uiState.value = _uiState.value.copy(
                        isAddProfileDialogOpen = false,
                        isCreatingProfile = false,
                        activeProfile = created,
                        pinSuccessMessage = "Created profile '${created.name}' and activated"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingProfile = false,
                        newProfileErrorMessage = err.localizedMessage ?: "Failed to create profile"
                    )
                }
            )
        }
    }

    // Delete Single Profile Handlers
    fun confirmDeleteProfile(profile: Profile) {
        _uiState.value = _uiState.value.copy(profileToDelete = profile)
    }

    fun dismissDeleteProfile() {
        _uiState.value = _uiState.value.copy(profileToDelete = null)
    }

    fun executeDeleteProfile() {
        val target = _uiState.value.profileToDelete ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, profileToDelete = null)
            val result = deleteProfileUseCase(target.id)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinSuccessMessage = "Deleted profile '${target.name}'"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinErrorMessage = err.localizedMessage ?: "Failed to delete profile"
                    )
                }
            )
        }
    }

    // Delete All Data & Account Reset Handlers
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
