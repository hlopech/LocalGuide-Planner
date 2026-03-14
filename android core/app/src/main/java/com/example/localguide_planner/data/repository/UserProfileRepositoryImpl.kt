package com.example.localguide_planner.data.repository

import com.example.localguide_planner.data.local.datastore.UserProfileDataStore
import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.repository.UserProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserProfileRepositoryImpl @Inject constructor(
    private val dataStore: UserProfileDataStore
) : UserProfileRepository {

    override fun getUserProfile(): Flow<UserProfile> = dataStore.getUserProfile()

    override suspend fun saveUserProfile(profile: UserProfile) =
        dataStore.saveUserProfile(profile)

    override suspend fun isOnboardingCompleted(): Boolean =
        dataStore.isOnboardingCompleted()
}
