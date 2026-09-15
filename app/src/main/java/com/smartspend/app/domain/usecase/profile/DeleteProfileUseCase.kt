package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class DeleteProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(profileId: String): Result<Unit> {
        return try {
            val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
            if (profiles.size <= 1) {
                return Result.failure(IllegalStateException("Cannot delete the only profile. Use account reset instead."))
            }

            val activeId = preferencesManager.activeProfileIdFlow.firstOrNull()
            profileRepository.deleteProfile(profileId)

            if (activeId == profileId) {
                val remaining = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
                val nextActive = remaining.firstOrNull { it.id != profileId } ?: remaining.firstOrNull()
                if (nextActive != null) {
                    preferencesManager.setActiveProfileId(nextActive.id)
                } else {
                    preferencesManager.setActiveProfileId(null)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
