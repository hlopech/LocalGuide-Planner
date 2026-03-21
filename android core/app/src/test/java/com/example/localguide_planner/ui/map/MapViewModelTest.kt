package com.example.localguide_planner.ui.map

import app.cash.turbine.test
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
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
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPlacesUseCase: GetPlacesUseCase = mockk()
    private lateinit var viewModel: MapViewModel

    private val placesWithCoordinates = listOf(
        Place(
            id = "1",
            name = "Центральный парк",
            description = "Описание парка",
            category = PlaceCategory.PARK,
            address = "ул. Ленина, 1",
            latitude = 55.75,
            longitude = 37.62,
            isFavorite = false,
            createdAt = 1_000_000L,
        ),
        Place(
            id = "2",
            name = "Городской музей",
            description = "Описание музея",
            category = PlaceCategory.MUSEUM,
            address = "пр. Победы, 10",
            latitude = 55.80,
            longitude = 37.65,
            isFavorite = true,
            createdAt = 2_000_000L,
        ),
    )

    private val placeWithoutCoordinates = Place(
        id = "3",
        name = "Кофейня без координат",
        description = "Описание",
        category = PlaceCategory.CAFE,
        address = "ул. Мира, 5",
        latitude = null,
        longitude = null,
        isFavorite = false,
        createdAt = 3_000_000L,
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
        viewModel = MapViewModel(getPlacesUseCase)

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.places.isEmpty())
    }

    @Test
    fun `loadPlaces_returnsAllPlaces`() = runTest {
        val allPlaces = placesWithCoordinates + listOf(placeWithoutCoordinates)
        every { getPlacesUseCase() } returns flowOf(allPlaces)
        viewModel = MapViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(3, loaded.places.size)
            assertTrue(loaded.places.any { it.id == "1" })
            assertTrue(loaded.places.any { it.id == "2" })
            assertTrue(loaded.places.any { it.id == "3" })
            assertNull(loaded.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emptyList_returnsEmptyState`() = runTest {
        every { getPlacesUseCase() } returns flowOf(emptyList())
        viewModel = MapViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val empty = awaitItem()
            assertFalse(empty.isLoading)
            assertTrue(empty.places.isEmpty())
            assertNull(empty.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits error state when flow throws exception`() = runTest {
        val errorMessage = "Ошибка загрузки данных"
        every { getPlacesUseCase() } returns flow { throw RuntimeException(errorMessage) }
        viewModel = MapViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val error = awaitItem()
            assertFalse(error.isLoading)
            assertNotNull(error.errorMessage)
            assertEquals(errorMessage, error.errorMessage)
            assertTrue(error.places.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `places include entries with null coordinates`() = runTest {
        val mixed = listOf(placesWithCoordinates[0], placeWithoutCoordinates)
        every { getPlacesUseCase() } returns flowOf(mixed)
        viewModel = MapViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            awaitItem() // loading

            val loaded = awaitItem()
            assertEquals(2, loaded.places.size)
            val noCoords = loaded.places.firstOrNull { it.id == "3" }
            assertNotNull(noCoords)
            assertNull(noCoords?.latitude)
            assertNull(noCoords?.longitude)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
