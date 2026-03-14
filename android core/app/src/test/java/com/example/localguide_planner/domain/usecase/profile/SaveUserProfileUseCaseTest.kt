package com.example.localguide_planner.domain.usecase.profile

import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveUserProfileUseCaseTest {

    private val repository: UserProfileRepository = mockk()
    private val useCase = SaveUserProfileUseCase(repository)

    @Test
    fun `invoke calls repository saveUserProfile with correct profile`() = runTest {
        val profile = UserProfile(name = "Bob", isOnboardingCompleted = false)
        coEvery { repository.saveUserProfile(profile) } returns Unit

        useCase(profile)

        coVerify(exactly = 1) { repository.saveUserProfile(profile) }
    }
}
