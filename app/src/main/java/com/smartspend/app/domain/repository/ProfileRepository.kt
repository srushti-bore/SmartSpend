package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<Profile>>
    suspend fun getProfileById(id: String): Profile?
    fun getProfileFlowById(id: String): Flow<Profile?>
    suspend fun createProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(id: String)
}
