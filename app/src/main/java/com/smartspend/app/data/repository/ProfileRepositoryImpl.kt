package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.ProfileDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProfileById(id: String): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    override fun getProfileFlowById(id: String): Flow<Profile?> {
        return profileDao.getProfileFlowById(id).map { it?.toDomain() }
    }

    override suspend fun createProfile(profile: Profile) {
        profileDao.insertProfile(profile.toEntity())
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(id: String) {
        profileDao.deleteProfileAndAllData(id)
    }
}
