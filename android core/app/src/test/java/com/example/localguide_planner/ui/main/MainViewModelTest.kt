package com.example.localguide_planner.ui.main

import com.example.localguide_planner.domain.usecase.profile.IsOnboardingCompletedUseCase
import com.example.localguide_planner.ui.navigation.Screen
import io.mockk.coEvery
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
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val isOnboardingCompletedUseCase: IsOnboardingCompletedUseCase = mockk()
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startDestination is null before coroutine completes`() {
        coEvery { isOnboardingCompletedUseCase() } returns true
        viewModel = MainViewModel(isOnboardingCompletedUseCase)

        assertNull(viewModel.startDestination.value)
    }

    @Test
    fun `startDestination is Main route when onboarding is completed`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns true
        viewModel = MainViewModel(isOnboardingCompletedUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(Screen.Main.route, viewModel.startDestination.value)
    }

    @Test
    fun `startDestination is Onboarding route when onboarding is not completed`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns false
        viewModel = MainViewModel(isOnboardingCompletedUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(Screen.Onboarding.route, viewModel.startDestination.value)
    }
}
