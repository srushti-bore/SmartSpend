package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.core.database.SmartSpendDatabase
import com.smartspend.app.core.datastore.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WipeDataUseCase @Inject constructor(
    private val database: SmartSpendDatabase,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                database.clearAllTables()
                preferencesManager.setActiveProfileId(null)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
