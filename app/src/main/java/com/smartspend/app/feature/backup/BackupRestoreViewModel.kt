package com.smartspend.app.feature.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class BackupRestoreUiState(
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val backupFileCreated: File? = null,
    val restoreSummary: RestoreSummary? = null,
    val errorMessage: String? = null
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
            activeProfileId = preferencesManager.activeProfileIdFlow.firstOrNull()
        }
    }

    fun exportEncryptedBackup(context: Context) {
        val profileId = activeProfileId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, errorMessage = null, backupFileCreated = null)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(context.cacheDir, "smartspend_backup_$timeStamp.smartspend")

            val result = encryptedBackupUseCase.createBackup(profileId, backupFile)
            result.fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        backupFileCreated = file
                    )
                    shareBackupFile(context, file)
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

    fun restoreFromBackupFile(context: Context, uri: Uri) {
        val profileId = activeProfileId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, errorMessage = null, restoreSummary = null)

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        errorMessage = "Could not read backup file"
                    )
                    return@launch
                }

                val result = encryptedRestoreUseCase.restoreBackup(profileId, inputStream)
                result.fold(
                    onSuccess = { summary ->
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            restoreSummary = summary
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            errorMessage = "Restore failed: ${error.localizedMessage ?: error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    errorMessage = e.localizedMessage ?: "Failed to restore backup"
                )
            }
        }
    }

    private fun shareBackupFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save Encrypted SmartSpend Backup"))
        } catch (_: Exception) {}
    }
}
