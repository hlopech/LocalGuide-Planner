package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.repository.UserProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.getUserProfile()
}
