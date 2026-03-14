package com.example.localguide_planner.ui.onboarding

import app.cash.turbine.test
import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.usecase.profile.SaveUserProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val saveUserProfileUseCase: SaveUserProfileUseCase = mockk()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(saveUserProfileUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onNameChanged updates name and clears error`() = runTest {
        viewModel.onNameChanged("Алексей")

        val state = viewModel.uiState.value
        assertEquals("Алексей", state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `onGetStartedClicked with blank name sets nameError to empty error key`() = runTest {
        viewModel.onNameChanged("")
        viewModel.onGetStartedClicked()

        val state = viewModel.uiState.value
        assertEquals(OnboardingViewModel.NAME_ERROR_EMPTY, state.nameError)
    }

    @Test
    fun `onGetStartedClicked with whitespace-only name sets nameError to empty error key`() = runTest {
        viewModel.onNameChanged("   ")
        viewModel.onGetStartedClicked()

        val state = viewModel.uiState.value
        assertEquals(OnboardingViewModel.NAME_ERROR_EMPTY, state.nameError)
    }

    @Test
    fun `onGetStartedClicked with name exceeding 50 chars sets nameError to too-long key`() = runTest {
        val longName = "А".repeat(51)
        viewModel.onNameChanged(longName)
        viewModel.onGetStartedClicked()

        val state = viewModel.uiState.value
        assertEquals(OnboardingViewModel.NAME_ERROR_TOO_LONG, state.nameError)
    }

    @Test
    fun `onGetStartedClicked with valid name calls SaveUserProfileUseCase with correct data`() = runTest {
        val expectedProfile = UserProfile(name = "Алексей", isOnboardingCompleted = true)
        coEvery { saveUserProfileUseCase(expectedProfile) } returns Unit

        viewModel.onNameChanged("Алексей")
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserProfileUseCase(expectedProfile) }
    }

    @Test
    fun `onGetStartedClicked trims whitespace before saving profile`() = runTest {
        val expectedProfile = UserProfile(name = "Алексей", isOnboardingCompleted = true)
        coEvery { saveUserProfileUseCase(expectedProfile) } returns Unit

        viewModel.onNameChanged("  Алексей  ")
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveUserProfileUseCase(expectedProfile) }
    }

    @Test
    fun `onGetStartedClicked with valid name emits NavigateToMain effect`() = runTest {
        val profile = UserProfile(name = "Алексей", isOnboardingCompleted = true)
        coEvery { saveUserProfileUseCase(profile) } returns Unit

        viewModel.uiEffect.test {
            viewModel.onNameChanged("Алексей")
            viewModel.onGetStartedClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(OnboardingUiEffect.NavigateToMain, awaitItem())
        }
    }

    @Test
    fun `onGetStartedClicked sets isLoading false after save completes`() = runTest {
        val profile = UserProfile(name = "Алексей", isOnboardingCompleted = true)
        coEvery { saveUserProfileUseCase(profile) } returns Unit

        viewModel.onNameChanged("Алексей")
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterIdle = viewModel.uiState.value
        assertEquals(false, stateAfterIdle.isLoading)
    }

    @Test
    fun `initial state has empty name no error and not loading`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertNull(state.nameError)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `onNameChanged after error clears the error`() = runTest {
        // First trigger an error
        viewModel.onGetStartedClicked()
        assertEquals(OnboardingViewModel.NAME_ERROR_EMPTY, viewModel.uiState.value.nameError)

        // Then type something - error should clear
        viewModel.onNameChanged("А")
        assertNull(viewModel.uiState.value.nameError)
    }
}
