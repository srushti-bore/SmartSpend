package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.core.security.HashUtils
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileCredentialUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profileId: String,
        newCredential: String,
        newAuthType: AuthType? = null
    ): Result<Unit> {
        val trimmed = newCredential.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Credential cannot be empty"))
        }

        val profile = profileRepository.getProfileById(profileId)
            ?: return Result.failure(IllegalStateException("Profile not found"))

        val newSalt = HashUtils.generateSalt()
        val newHash = HashUtils.hashCredential(trimmed, newSalt)

        val updatedProfile = profile.copy(
            primaryAuthType = newAuthType ?: profile.primaryAuthType,
            credentialSalt = newSalt,
            credentialHash = newHash
        )

        profileRepository.updateProfile(updatedProfile)
        return Result.success(Unit)
    }
}
