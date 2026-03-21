package com.example.localguide_planner.ui.places.favorites

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
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPlacesUseCase: GetPlacesUseCase = mockk()

    private lateinit var viewModel: FavoritesViewModel

    private val allPlaces = listOf(
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
        Place(
            id = "3",
            name = "Городской музей",
            description = "Описание музея",
            category = PlaceCategory.MUSEUM,
            address = "ул. Победы, 3",
            latitude = 55.80,
            longitude = 37.65,
            isFavorite = true,
            createdAt = 3_000_000L,
        ),
        Place(
            id = "4",
            name = "Ресторан «Восток»",
            description = "Восточная кухня",
            category = PlaceCategory.RESTAURANT,
            address = "пр. Мира, 10",
            latitude = null,
            longitude = null,
            isFavorite = false,
            createdAt = 4_000_000L,
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
        viewModel = FavoritesViewModel(getPlacesUseCase)

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.places.isEmpty())
    }

    @Test
    fun `uiState filters only favorite places`() = runTest {
        every { getPlacesUseCase() } returns flowOf(allPlaces)
        viewModel = FavoritesViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val listState = awaitItem()
            assertFalse(listState.isLoading)
            assertEquals(2, listState.places.size)
            assertTrue(listState.places.all { it.isFavorite })
            val ids = listState.places.map { it.id }
            assertTrue(ids.contains("1"))
            assertTrue(ids.contains("3"))
            assertNull(listState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState shows empty list when no favorites`() = runTest {
        val noFavorites = allPlaces.map { it.copy(isFavorite = false) }
        every { getPlacesUseCase() } returns flowOf(noFavorites)
        viewModel = FavoritesViewModel(getPlacesUseCase)

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
    fun `uiState emits error when flow throws exception`() = runTest {
        val errorMessage = "Ошибка загрузки"
        every { getPlacesUseCase() } returns flow { throw RuntimeException(errorMessage) }
        viewModel = FavoritesViewModel(getPlacesUseCase)

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
    fun `onCategorySelected filters favorites by category`() = runTest {
        every { getPlacesUseCase() } returns flowOf(allPlaces)
        viewModel = FavoritesViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full favorites list

            viewModel.onCategorySelected(PlaceCategory.PARK)
            val filtered = awaitItem()

            assertEquals(1, filtered.places.size)
            assertEquals("1", filtered.places[0].id)
            assertEquals(PlaceCategory.PARK, filtered.selectedCategory)
            assertTrue(filtered.places[0].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCategorySelected with null clears filter and shows all favorites`() = runTest {
        every { getPlacesUseCase() } returns flowOf(allPlaces)
        viewModel = FavoritesViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full favorites list

            viewModel.onCategorySelected(PlaceCategory.PARK)
            val filtered = awaitItem()
            assertEquals(1, filtered.places.size)

            viewModel.onCategorySelected(null)
            val allFavorites = awaitItem()

            assertEquals(2, allFavorites.places.size)
            assertNull(allFavorites.selectedCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCategorySelected for category with no favorites shows empty list`() = runTest {
        every { getPlacesUseCase() } returns flowOf(allPlaces)
        viewModel = FavoritesViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full favorites list

            viewModel.onCategorySelected(PlaceCategory.CAFE)
            val filtered = awaitItem()

            assertTrue(filtered.places.isEmpty())
            assertEquals(PlaceCategory.CAFE, filtered.selectedCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState reacts reactively when places list changes`() = runTest {
        val initialPlaces = allPlaces.map { it.copy(isFavorite = false) }
        // Only id="1" becomes favorite, all others stay non-favorite
        val updatedPlaces = allPlaces.map { it.copy(isFavorite = it.id == "1") }

        val placesFlow = kotlinx.coroutines.flow.MutableStateFlow(initialPlaces)
        every { getPlacesUseCase() } returns placesFlow
        viewModel = FavoritesViewModel(getPlacesUseCase)

        viewModel.uiState.test {
            // consume all items until stable empty state
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertTrue(state.places.isEmpty())

            placesFlow.value = updatedPlaces
            val updatedState = awaitItem()

            assertEquals(1, updatedState.places.size)
            assertEquals("1", updatedState.places[0].id)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
