package com.example.localguide_planner.domain.repository

import com.example.localguide_planner.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun isOnboardingCompleted(): Boolean
}
