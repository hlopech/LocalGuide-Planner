package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsOnboardingCompletedUseCaseTest {

    private val repository: UserProfileRepository = mockk()
    private val useCase = IsOnboardingCompletedUseCase(repository)

    @Test
    fun `invoke returns true when onboarding is completed`() = runTest {
        coEvery { repository.isOnboardingCompleted() } returns true

        val result = useCase()

        assertTrue(result)
    }

    @Test
    fun `invoke returns false when onboarding is not completed`() = runTest {
        coEvery { repository.isOnboardingCompleted() } returns false

        val result = useCase()

        assertFalse(result)
    }
}
