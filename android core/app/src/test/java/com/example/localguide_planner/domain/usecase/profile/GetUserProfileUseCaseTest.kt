package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.repository.UserProfileRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUserProfileUseCaseTest {

    private val repository: UserProfileRepository = mockk()
    private val useCase = GetUserProfileUseCase(repository)

    @Test
    fun `invoke returns user profile flow from repository`() = runTest {
        val profile = UserProfile(name = "Alice", isOnboardingCompleted = true)
        every { repository.getUserProfile() } returns flowOf(profile)

        val result = useCase().first()

        assertEquals(profile, result)
    }
}
