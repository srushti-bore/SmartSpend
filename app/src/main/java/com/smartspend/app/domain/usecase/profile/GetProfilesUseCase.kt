package com.smartspend.app.domain.usecase.profile

import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfilesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<List<Profile>> {
        return profileRepository.getAllProfiles()
    }
}
