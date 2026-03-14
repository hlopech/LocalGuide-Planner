package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.repository.UserProfileRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) = repository.saveUserProfile(profile)
}
