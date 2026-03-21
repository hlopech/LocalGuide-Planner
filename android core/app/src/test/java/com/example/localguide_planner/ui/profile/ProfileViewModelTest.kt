package com.example.localguide_planner.ui.profile

import app.cash.turbine.test
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.usecase.place.DeleteAllPlacesUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import com.example.localguide_planner.domain.usecase.profile.GetUserProfileUseCase
import com.example.localguide_planner.domain.usecase.profile.SaveUserProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val saveUserProfileUseCase: SaveUserProfileUseCase = mockk()
    private val getPlacesUseCase: GetPlacesUseCase = mockk()
    private val deleteAllPlacesUseCase: DeleteAllPlacesUseCase = mockk()

    private val testProfile = UserProfile(name = "Алексей", isOnboardingCompleted = true)

    private val testPlaces = listOf(
        Place(
            id = "1",
            name = "Центральный парк",
            description = "Описание",
            category = PlaceCategory.PARK,
            address = "ул. Ленина, 1",
            latitude = 55.75,
            longitude = 37.62,
            isFavorite = true,
            createdAt = 1_000_000L,
        ),
        Place(
            id = "2",
            name = "Кофейня Уют",
            description = "Описание кофейни",
            category = PlaceCategory.CAFE,
            address = "пр. Победы, 15",
            latitude = null,
            longitude = null,
            isFavorite = false,
            createdAt = 2_000_000L,
        ),
        Place(
            id = "3",
            name = "Кофе Хаус",
            description = "Ещё кофейня",
            category = PlaceCategory.CAFE,
            address = "ул. Садовая, 5",
            latitude = null,
            longitude = null,
            isFavorite = true,
            createdAt = 3_000_000L,
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            saveUserProfileUseCase = saveUserProfileUseCase,
            getPlacesUseCase = getPlacesUseCase,
            deleteAllPlacesUseCase = deleteAllPlacesUseCase,
        )
    }

    @Test
    fun `loadProfile_returnsCorrectState`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(testPlaces)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals("Алексей", loaded.userName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNameChanged_updatesEditedName`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.onNameChanged("Новое имя")
            val updated = awaitItem()
            assertEquals("Новое имя", updated.editedName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveProfile_callsSaveUseCase`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(emptyList())
        coEvery { saveUserProfileUseCase(any()) } returns Unit

        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // loaded
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onNameChanged("Новое имя")
        viewModel.saveProfile()
        advanceUntilIdle()

        coVerify(exactly = 1) { saveUserProfileUseCase(any()) }
    }

    @Test
    fun `totalPlacesCount_isCorrect`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(testPlaces)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading

            val loaded = awaitItem()
            assertEquals(3, loaded.totalPlacesCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favoritesCount_isCorrect`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(testPlaces)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading

            val loaded = awaitItem()
            assertEquals(2, loaded.favoritePlacesCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `topCategory_isCalculatedCorrectly`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(testPlaces)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading

            val loaded = awaitItem()
            // CAFE appears 2 times, PARK appears 1 time — top category should be CAFE
            assertEquals(PlaceCategory.CAFE, loaded.topCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `topCategory_isNull_whenNoPlaces`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading

            val loaded = awaitItem()
            assertNull(loaded.topCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetAllData_callsDeleteAllUseCase`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(testPlaces)
        coEvery { deleteAllPlacesUseCase() } returns Unit

        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // loaded
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.resetAllData()
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteAllPlacesUseCase() }
    }

    @Test
    fun `showResetDialog_setsShowResetConfirmDialog_true`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.showResetDialog()
            val dialogState = awaitItem()
            assertTrue(dialogState.showResetConfirmDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissResetDialog_setsShowResetConfirmDialog_false`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(testProfile)
        every { getPlacesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.showResetDialog()
            awaitItem() // dialog shown

            viewModel.dismissResetDialog()
            val dismissed = awaitItem()
            assertFalse(dismissed.showResetConfirmDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
