package com.example.localguide_planner.ui.places.list

import app.cash.turbine.test
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.DeletePlaceUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class PlacesListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPlacesUseCase: GetPlacesUseCase = mockk()
    private val deletePlaceUseCase: DeletePlaceUseCase = mockk()

    private lateinit var viewModel: PlacesListViewModel

    private val samplePlaces = listOf(
        Place(
            id = "1",
            name = "Центральный парк",
            description = "Описание парка",
            category = PlaceCategory.PARK,
            address = "ул. Ленина, 1",
            latitude = 55.75,
            longitude = 37.62,
            isFavorite = true,
            createdAt = 1_000_000L,
        ),
        Place(
            id = "2",
            name = "Кофейня «Уют»",
            description = "Описание кофейни",
            category = PlaceCategory.CAFE,
            address = "пр. Победы, 15",
            latitude = null,
            longitude = null,
            isFavorite = false,
            createdAt = 2_000_000L,
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

    @Test
    fun `initial uiState has isLoading true`() {
        every { getPlacesUseCase() } returns flowOf(emptyList())
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.places.isEmpty())
    }

    @Test
    fun `uiState transitions from loading to list when places emitted`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            // Initial state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // After flow emits
            val listState = awaitItem()
            assertFalse(listState.isLoading)
            assertEquals(samplePlaces, listState.places)
            assertNull(listState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState transitions from loading to empty list`() = runTest {
        every { getPlacesUseCase() } returns flowOf(emptyList())
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val emptyState = awaitItem()
            assertFalse(emptyState.isLoading)
            assertTrue(emptyState.places.isEmpty())
            assertNull(emptyState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits error state when flow throws exception`() = runTest {
        val errorMessage = "Ошибка загрузки"
        every { getPlacesUseCase() } returns flow { throw RuntimeException(errorMessage) }
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.errorMessage)
            assertEquals(errorMessage, errorState.errorMessage)
            assertTrue(errorState.places.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deletePlace calls deletePlaceUseCase with correct id`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        coEvery { deletePlaceUseCase(any()) } returns Unit
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.deletePlace("1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deletePlaceUseCase("1") }
    }

    @Test
    fun `deletePlace with different id calls useCase with that id`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        coEvery { deletePlaceUseCase(any()) } returns Unit
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.deletePlace("2")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deletePlaceUseCase("2") }
    }

    @Test
    fun `uiState places contain correct data after loading`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // skip loading state

            val listState = awaitItem()
            assertEquals(2, listState.places.size)
            assertEquals("1", listState.places[0].id)
            assertEquals("Центральный парк", listState.places[0].name)
            assertEquals(PlaceCategory.PARK, listState.places[0].category)
            assertTrue(listState.places[0].isFavorite)
            assertEquals("2", listState.places[1].id)
            assertFalse(listState.places[1].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
