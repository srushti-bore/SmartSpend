package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.core.security.HashUtils
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.ProfileRepository
import java.util.UUID
import javax.inject.Inject

class CreateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        name: String,
        authType: AuthType,
        rawCredential: String,
        biometricEnabled: Boolean
    ): Result<Profile> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Profile name cannot be empty"))
        }
        if (rawCredential.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Credential cannot be empty"))
        }

        val profileId = UUID.randomUUID().toString()
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashCredential(rawCredential.trim(), salt)

        val profile = Profile(
            id = profileId,
            name = trimmedName,
            primaryAuthType = authType,
            credentialSalt = salt,
            credentialHash = hash,
            biometricEnabled = biometricEnabled,
            createdAt = System.currentTimeMillis()
        )

        profileRepository.createProfile(profile)

        // Seed Starter Categories and Payment Methods
        categoryRepository.addCategories(Category.starterCategories(profileId))
        paymentMethodRepository.addPaymentMethods(PaymentMethod.starterPaymentMethods(profileId))

        return Result.success(profile)
    }
}
