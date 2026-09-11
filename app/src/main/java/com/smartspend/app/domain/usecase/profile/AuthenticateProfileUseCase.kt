package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.core.security.HashUtils
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import javax.inject.Inject

class AuthenticateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: String, rawCredential: String): Boolean {
        val profile = profileRepository.getProfileById(profileId) ?: return false
        return HashUtils.verifyCredential(rawCredential.trim(), profile.credentialSalt, profile.credentialHash)
    }
}
