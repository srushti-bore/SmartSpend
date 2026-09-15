package com.smartspend.app.feature.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.common.BackupStorageHelper
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.usecase.backup.EncryptedBackupUseCase
import com.smartspend.app.domain.usecase.backup.EncryptedRestoreUseCase
import com.smartspend.app.domain.usecase.backup.RestoreSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ExportActionType {
    SAVE_TO_URI,
    SAVE_TO_DOWNLOADS,
    SHARE
}

data class BackupRestoreUiState(
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val backupFileCreated: File? = null,
    val restoreSummary: RestoreSummary? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    // Export Password Dialog State
    val isExportPasswordDialogOpen: Boolean = false,
    val exportPasswordInput: String = "",
    val exportConfirmPasswordInput: String = "",
    val exportPasswordErrorMessage: String? = null,
    val pendingExportAction: ExportActionType? = null,
    val pendingDestinationUri: Uri? = null,
    // Restore Password Dialog State
    val isRestorePasswordDialogOpen: Boolean = false,
    val restorePasswordInput: String = "",
    val restorePasswordErrorMessage: String? = null,
    val pendingRestoreUri: Uri? = null
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val encryptedBackupUseCase: EncryptedBackupUseCase,
    private val encryptedRestoreUseCase: EncryptedRestoreUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { id ->
                activeProfileId = id
            }
        }
    }

    fun generateBackupFileName(): String {
        val timeStamp = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        return "expense_backup_$timeStamp.enc"
    }

    // ==========================================
    // EXPORT PASSWORD DIALOG HANDLERS
    // ==========================================
    fun openExportPasswordDialog(actionType: ExportActionType, destinationUri: Uri? = null) {
        _uiState.value = _uiState.value.copy(
            isExportPasswordDialogOpen = true,
            exportPasswordInput = "",
            exportConfirmPasswordInput = "",
            exportPasswordErrorMessage = null,
            pendingExportAction = actionType,
            pendingDestinationUri = destinationUri
        )
    }

    fun closeExportPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            isExportPasswordDialogOpen = false,
            exportPasswordErrorMessage = null,
            pendingExportAction = null,
            pendingDestinationUri = null
        )
    }

    fun onExportPasswordChange(pwd: String) {
        _uiState.value = _uiState.value.copy(exportPasswordInput = pwd, exportPasswordErrorMessage = null)
    }

    fun onExportConfirmPasswordChange(confirm: String) {
        _uiState.value = _uiState.value.copy(exportConfirmPasswordInput = confirm, exportPasswordErrorMessage = null)
    }

    fun confirmAndExecuteExport(context: Context) {
        val state = _uiState.value
        val pwd = state.exportPasswordInput.trim()
        val confirm = state.exportConfirmPasswordInput.trim()

        if (pwd.isEmpty()) {
            _uiState.value = state.copy(exportPasswordErrorMessage = "Please set a backup encryption password")
            return
        }
        if (pwd.length < 4) {
            _uiState.value = state.copy(exportPasswordErrorMessage = "Password must be at least 4 characters")
            return
        }
        if (pwd != confirm) {
            _uiState.value = state.copy(exportPasswordErrorMessage = "Passwords do not match")
            return
        }

        val action = state.pendingExportAction ?: ExportActionType.SAVE_TO_DOWNLOADS
        val uri = state.pendingDestinationUri

        _uiState.value = state.copy(isExportPasswordDialogOpen = false)

        when (action) {
            ExportActionType.SAVE_TO_URI -> {
                if (uri != null) {
                    saveBackupToUriWithPassword(context, uri, pwd)
                }
            }
            ExportActionType.SAVE_TO_DOWNLOADS -> {
                downloadBackupWithPassword(context, pwd)
            }
            ExportActionType.SHARE -> {
                shareBackupWithPassword(context, pwd)
            }
        }
    }

    /**
     * Saves encrypted backup directly into a SAF Document Uri with user's encryption password.
     */
    private fun saveBackupToUriWithPassword(context: Context, destinationUri: Uri, password: String) {
        viewModelScope.launch {
            val profileId = activeProfileId ?: preferencesManager.activeProfileIdFlow.firstOrNull()
            if (profileId == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "No active profile found for backup")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isExporting = true,
                errorMessage = null,
                successMessage = null,
                backupFileCreated = null
            )

            val fileName = generateBackupFileName()
            val tempFile = File(context.cacheDir, fileName)

            val result = encryptedBackupUseCase.createBackup(profileId, tempFile, password)
            result.fold(
                onSuccess = { file ->
                    val saveResult = BackupStorageHelper.saveToUri(context, file, destinationUri)
                    saveResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                backupFileCreated = file,
                                successMessage = "✓ Successfully password-encrypted and saved to your device"
                            )
                        },
                        onFailure = { err ->
                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                errorMessage = "Failed to save file: ${err.message}"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        errorMessage = error.localizedMessage ?: "Failed to export encrypted backup"
                    )
                }
            )
        }
    }

    /**
     * Directly saves encrypted backup to device's public Downloads directory with password.
     */
    private fun downloadBackupWithPassword(context: Context, password: String) {
        viewModelScope.launch {
            val profileId = activeProfileId ?: preferencesManager.activeProfileIdFlow.firstOrNull()
            if (profileId == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "No active profile found for backup")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isExporting = true,
                errorMessage = null,
                successMessage = null,
                backupFileCreated = null
            )

            val fileName = generateBackupFileName()
            val tempFile = File(context.cacheDir, fileName)

            val result = encryptedBackupUseCase.createBackup(profileId, tempFile, password)
            result.fold(
                onSuccess = { file ->
                    val saveResult = BackupStorageHelper.saveToDownloads(context, file, fileName)
                    saveResult.fold(
                        onSuccess = { path ->
                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                backupFileCreated = file,
                                successMessage = "✓ Successfully saved password-protected backup to $path"
                            )
                        },
                        onFailure = { err ->
                            _uiState.value = _uiState.value.copy(
                                isExporting = false,
                                errorMessage = "Failed to save to Downloads: ${err.message}"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        errorMessage = error.localizedMessage ?: "Failed to export encrypted backup"
                    )
                }
            )
        }
    }

    /**
     * Generates password-encrypted backup and opens Android Share Sheet.
     */
    private fun shareBackupWithPassword(context: Context, password: String) {
        viewModelScope.launch {
            val profileId = activeProfileId ?: preferencesManager.activeProfileIdFlow.firstOrNull()
            if (profileId == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "No active profile found for backup")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isExporting = true,
                errorMessage = null,
                successMessage = null,
                backupFileCreated = null
            )

            val fileName = generateBackupFileName()
            val tempFile = File(context.cacheDir, fileName)

            val result = encryptedBackupUseCase.createBackup(profileId, tempFile, password)
            result.fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        backupFileCreated = file,
                        successMessage = "Password-protected backup created (${file.length() / 1024} KB). Opening share sheet..."
                    )
                    BackupStorageHelper.shareFile(context, file, "Share SmartSpend Backup")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        errorMessage = error.localizedMessage ?: "Failed to export encrypted backup"
                    )
                }
            )
        }
    }

    // ==========================================
    // RESTORE PASSWORD DIALOG HANDLERS
    // ==========================================
    fun onRestoreFilePicked(context: Context, uri: Uri) {
        _uiState.value = _uiState.value.copy(
            isRestorePasswordDialogOpen = true,
            restorePasswordInput = "",
            restorePasswordErrorMessage = null,
            pendingRestoreUri = uri
        )
    }

    fun closeRestorePasswordDialog() {
        _uiState.value = _uiState.value.copy(
            isRestorePasswordDialogOpen = false,
            restorePasswordErrorMessage = null,
            pendingRestoreUri = null
        )
    }

    fun onRestorePasswordChange(pwd: String) {
        _uiState.value = _uiState.value.copy(restorePasswordInput = pwd, restorePasswordErrorMessage = null)
    }

    fun executeRestoreWithPassword(context: Context) {
        val state = _uiState.value
        val uri = state.pendingRestoreUri ?: return
        val password = state.restorePasswordInput.trim()

        viewModelScope.launch {
            val profileId = activeProfileId ?: preferencesManager.activeProfileIdFlow.firstOrNull()
            if (profileId == null) {
                _uiState.value = _uiState.value.copy(restorePasswordErrorMessage = "No active profile found for restore")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isRestoring = true,
                errorMessage = null,
                successMessage = null,
                restoreSummary = null
            )

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        restorePasswordErrorMessage = "Could not read backup file"
                    )
                    return@launch
                }

                val result = encryptedRestoreUseCase.restoreBackup(profileId, inputStream, password)
                result.fold(
                    onSuccess = { summary ->
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            isRestorePasswordDialogOpen = false,
                            restoreSummary = summary,
                            successMessage = "Restored ${summary.expensesCount} expenses & ${summary.incomesCount} incomes successfully."
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            restorePasswordErrorMessage = error.localizedMessage ?: "Incorrect password or corrupt file"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    restorePasswordErrorMessage = e.localizedMessage ?: "Failed to restore backup"
                )
            }
        }
    }
}
