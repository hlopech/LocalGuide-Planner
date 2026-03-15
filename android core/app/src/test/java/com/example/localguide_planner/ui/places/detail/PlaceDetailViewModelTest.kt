package com.example.localguide_planner.ui.places.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.DeletePlaceUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getPlaceByIdUseCase: GetPlaceByIdUseCase = mockk()
    private val deletePlaceUseCase: DeletePlaceUseCase = mockk()
    private val savePlaceUseCase: SavePlaceUseCase = mockk()

    private val samplePlace = Place(
        id = "1",
        name = "Центральный парк",
        description = "Красивый парк",
        category = PlaceCategory.PARK,
        address = "ул. Ленина, 1",
        latitude = 55.75,
        longitude = 37.62,
        isFavorite = false,
        createdAt = 1_000_000L,
    )

    private fun createViewModel(placeId: String = "1"): PlaceDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(Screen.PlaceDetail.ARG_PLACE_ID to placeId),
        )
        return PlaceDetailViewModel(
            savedStateHandle = savedStateHandle,
            getPlaceByIdUseCase = getPlaceByIdUseCase,
            deletePlaceUseCase = deletePlaceUseCase,
            savePlaceUseCase = savePlaceUseCase,
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Loading`() {
        coEvery { getPlaceByIdUseCase(any()) } returns samplePlace
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is PlaceDetailUiState.Loading)
    }

    @Test
    fun `uiState transitions to Success when place exists`() = runTest {
        coEvery { getPlaceByIdUseCase("1") } returns samplePlace
        val viewModel = createViewModel(placeId = "1")

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading is PlaceDetailUiState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val success = awaitItem()
            assertTrue(success is PlaceDetailUiState.Success)
            assertEquals(samplePlace, (success as PlaceDetailUiState.Success).place)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState transitions to Error when place does not exist`() = runTest {
        coEvery { getPlaceByIdUseCase("unknown") } returns null
        val viewModel = createViewModel(placeId = "unknown")

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading is PlaceDetailUiState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val error = awaitItem()
            assertTrue(error is PlaceDetailUiState.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteClicked calls deletePlaceUseCase and emits NavigateBack`() = runTest {
        coEvery { getPlaceByIdUseCase("1") } returns samplePlace
        coEvery { deletePlaceUseCase("1") } returns Unit
        val viewModel = createViewModel(placeId = "1")

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onDeleteClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PlaceDetailUiEffect.NavigateBack)
            coVerify(exactly = 1) { deletePlaceUseCase("1") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onToggleFavorite saves place with toggled isFavorite and reloads`() = runTest {
        val updatedPlace = samplePlace.copy(isFavorite = true)
        coEvery { getPlaceByIdUseCase("1") } returnsMany listOf(samplePlace, updatedPlace)
        coEvery { savePlaceUseCase(any()) } returns Unit
        val viewModel = createViewModel(placeId = "1")

        testDispatcher.scheduler.advanceUntilIdle()

        val successState = viewModel.uiState.value as PlaceDetailUiState.Success
        assertFalse(successState.place.isFavorite)

        viewModel.onToggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { savePlaceUseCase(samplePlace.copy(isFavorite = true)) }
        val newState = viewModel.uiState.value as PlaceDetailUiState.Success
        assertTrue(newState.place.isFavorite)
    }

    @Test
    fun `onToggleFavorite does nothing when uiState is not Success`() = runTest {
        coEvery { getPlaceByIdUseCase("1") } returns samplePlace
        val viewModel = createViewModel(placeId = "1")

        // State is Loading at this point, do NOT advance scheduler
        viewModel.onToggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { savePlaceUseCase(any()) }
    }

    @Test
    fun `onEditClicked emits NavigateToEdit with correct placeId`() = runTest {
        coEvery { getPlaceByIdUseCase("1") } returns samplePlace
        val viewModel = createViewModel(placeId = "1")

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEditClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PlaceDetailUiEffect.NavigateToEdit)
            assertEquals("1", (effect as PlaceDetailUiEffect.NavigateToEdit).placeId)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
