package com.example.localguide_planner.data.repository

import com.example.localguide_planner.data.local.datastore.UserProfileDataStore
import com.example.localguide_planner.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileRepositoryImplTest {

    private val dataStore: UserProfileDataStore = mockk(relaxed = true)
    private val repository = UserProfileRepositoryImpl(dataStore)

    @Test
    fun `getUserProfile delegates to dataStore`() = runTest {
        val profile = UserProfile(name = "Alice", isOnboardingCompleted = true)
        every { dataStore.getUserProfile() } returns flowOf(profile)

        val result = repository.getUserProfile().first()

        assertEquals(profile, result)
    }

    @Test
    fun `getUserProfile returns default values when dataStore is empty`() = runTest {
        val defaultProfile = UserProfile(name = "", isOnboardingCompleted = false)
        every { dataStore.getUserProfile() } returns flowOf(defaultProfile)

        val result = repository.getUserProfile().first()

        assertEquals("", result.name)
        assertFalse(result.isOnboardingCompleted)
    }

    @Test
    fun `saveUserProfile calls dataStore with correct profile`() = runTest {
        val profile = UserProfile(name = "Bob", isOnboardingCompleted = false)
        val profileSlot = slot<UserProfile>()
        coEvery { dataStore.saveUserProfile(capture(profileSlot)) } returns Unit

        repository.saveUserProfile(profile)

        coVerify(exactly = 1) { dataStore.saveUserProfile(any()) }
        assertEquals("Bob", profileSlot.captured.name)
        assertFalse(profileSlot.captured.isOnboardingCompleted)
    }

    @Test
    fun `isOnboardingCompleted delegates to dataStore and returns true`() = runTest {
        coEvery { dataStore.isOnboardingCompleted() } returns true

        val result = repository.isOnboardingCompleted()

        assertTrue(result)
    }

    @Test
    fun `isOnboardingCompleted delegates to dataStore and returns false`() = runTest {
        coEvery { dataStore.isOnboardingCompleted() } returns false

        val result = repository.isOnboardingCompleted()

        assertFalse(result)
    }

    @Test
    fun `saveUserProfile with onboarding completed flag is forwarded correctly`() = runTest {
        val profile = UserProfile(name = "Carol", isOnboardingCompleted = true)
        val profileSlot = slot<UserProfile>()
        coEvery { dataStore.saveUserProfile(capture(profileSlot)) } returns Unit

        repository.saveUserProfile(profile)

        assertTrue(profileSlot.captured.isOnboardingCompleted)
        assertEquals("Carol", profileSlot.captured.name)
    }
}
