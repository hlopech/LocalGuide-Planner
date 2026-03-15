package com.example.localguide_planner.ui.places.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.GetPlaceByIdUseCase
import com.example.localguide_planner.domain.usecase.place.SavePlaceUseCase
import com.example.localguide_planner.ui.navigation.Screen
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditPlaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getPlaceByIdUseCase: GetPlaceByIdUseCase = mockk()
    private val savePlaceUseCase: SavePlaceUseCase = mockk()

    private val samplePlace = Place(
        id = "place-123",
        name = "Центральный парк",
        description = "Красивый парк",
        category = PlaceCategory.PARK,
        address = "ул. Ленина, 1",
        latitude = 55.75,
        longitude = 37.62,
        isFavorite = true,
        createdAt = 1_000_000L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(placeId: String? = null): AddEditPlaceViewModel {
        val map = if (placeId != null) {
            mapOf(Screen.EditPlace.ARG_PLACE_ID to placeId)
        } else {
            emptyMap()
        }
        return AddEditPlaceViewModel(
            savedStateHandle = SavedStateHandle(map),
            getPlaceByIdUseCase = getPlaceByIdUseCase,
            savePlaceUseCase = savePlaceUseCase,
        )
    }

    // Test 1: validation — empty name sets nameError, UseCase not called
    @Test
    fun `onSaveClicked with empty name emits nameError`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { savePlaceUseCase(any()) }
    }

    // Test 2: happy path add — valid data calls savePlaceUseCase
    @Test
    fun `onSaveClicked with valid data calls savePlaceUseCase`() = runTest {
        coEvery { savePlaceUseCase(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.onNameChange("Новое место")
        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { savePlaceUseCase(any()) }
    }

    // Test 3: NavigateBack emitted after successful save
    @Test
    fun `onSaveClicked emits NavigateBack after save`() = runTest {
        coEvery { savePlaceUseCase(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.onNameChange("Новое место")

        viewModel.uiEffect.test {
            viewModel.onSaveClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AddEditPlaceUiEffect.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test 4: init with placeId loads place and sets isEditMode = true
    @Test
    fun `init with placeId loads place and sets isEditMode true`() = runTest {
        coEvery { getPlaceByIdUseCase("place-123") } returns samplePlace
        val viewModel = createViewModel(placeId = "place-123")

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals(samplePlace.name, state.name)
        assertEquals(samplePlace.address, state.address)
        assertEquals(samplePlace.description, state.description)
        assertEquals(samplePlace.category, state.category)
    }

    // Test 5: init with null placeId keeps isEditMode false
    @Test
    fun `init with null placeId keeps isEditMode false`() = runTest {
        val viewModel = createViewModel(placeId = null)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditMode)
        assertEquals("", state.name)
        assertEquals("", state.address)
    }

    // Test 6: init with unknown placeId emits NavigateBack
    @Test
    fun `init with unknown placeId emits NavigateBack`() = runTest {
        coEvery { getPlaceByIdUseCase("unknown-id") } returns null
        val viewModel = createViewModel(placeId = "unknown-id")

        viewModel.uiEffect.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AddEditPlaceUiEffect.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test 7: edit mode preserves original id and createdAt
    @Test
    fun `onSaveClicked in edit mode preserves original id and createdAt`() = runTest {
        coEvery { getPlaceByIdUseCase("place-123") } returns samplePlace
        coEvery { savePlaceUseCase(any()) } returns Unit
        val viewModel = createViewModel(placeId = "place-123")

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChange("Обновлённое имя")
        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            savePlaceUseCase(
                match { place ->
                    place.id == samplePlace.id &&
                        place.createdAt == samplePlace.createdAt &&
                        place.isFavorite == samplePlace.isFavorite &&
                        place.name == "Обновлённое имя"
                },
            )
        }
    }

    // Test 8: onNameChange clears nameError
    @Test
    fun `onNameChange clears nameError`() = runTest {
        val viewModel = createViewModel()

        // Trigger validation error
        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.nameError)

        // Fix the name
        viewModel.onNameChange("Имя")
        assertNull(viewModel.uiState.value.nameError)
    }
}
