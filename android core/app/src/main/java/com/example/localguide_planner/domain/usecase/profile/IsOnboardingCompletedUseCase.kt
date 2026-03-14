package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.repository.UserProfileRepository
import javax.inject.Inject

class IsOnboardingCompletedUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): Boolean = repository.isOnboardingCompleted()
}
