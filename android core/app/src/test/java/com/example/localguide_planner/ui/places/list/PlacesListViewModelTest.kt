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
import kotlinx.coroutines.test.advanceUntilIdle
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
        Place(
            id = "3",
            name = "Городской музей",
            description = "Описание музея",
            category = PlaceCategory.MUSEUM,
            address = "ул. Победы, 3",
            latitude = 55.80,
            longitude = 37.65,
            isFavorite = false,
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
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

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
            assertEquals(3, listState.places.size)
            assertEquals("1", listState.places[0].id)
            assertEquals("Центральный парк", listState.places[0].name)
            assertEquals(PlaceCategory.PARK, listState.places[0].category)
            assertTrue(listState.places[0].isFavorite)
            assertEquals("2", listState.places[1].id)
            assertFalse(listState.places[1].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchByName filtersCorrectly`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onSearchQueryChanged("парк")
            val filtered = awaitItem()

            assertEquals(1, filtered.places.size)
            assertEquals("1", filtered.places[0].id)
            assertEquals("парк", filtered.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchByAddress filtersCorrectly`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onSearchQueryChanged("Победы")
            val filtered = awaitItem()

            assertEquals(2, filtered.places.size)
            val ids = filtered.places.map { it.id }
            assertTrue(ids.contains("2"))
            assertTrue(ids.contains("3"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filterByCategory filtersCorrectly`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onCategorySelected(PlaceCategory.CAFE)
            val filtered = awaitItem()

            assertEquals(1, filtered.places.size)
            assertEquals("2", filtered.places[0].id)
            assertEquals(PlaceCategory.CAFE, filtered.selectedCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `combinedFilter andLogic`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onCategorySelected(PlaceCategory.MUSEUM)
            awaitItem() // museum filter applied

            viewModel.onSearchQueryChanged("Победы")
            val filtered = awaitItem()

            assertEquals(1, filtered.places.size)
            assertEquals("3", filtered.places[0].id)
            assertEquals(PlaceCategory.MUSEUM, filtered.selectedCategory)
            assertEquals("Победы", filtered.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearFilter showsAllPlaces`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onCategorySelected(PlaceCategory.PARK)
            val filteredState = awaitItem()
            assertEquals(1, filteredState.places.size)

            viewModel.onCategorySelected(null)
            val allPlacesState = awaitItem()

            assertEquals(samplePlaces.size, allPlacesState.places.size)
            assertNull(allPlacesState.selectedCategory)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `caseInsensitiveSearch`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onSearchQueryChanged("ПАРК")
            val upperCase = awaitItem()

            viewModel.onSearchQueryChanged("парк")
            val lowerCase = awaitItem()

            assertEquals(upperCase.places.size, lowerCase.places.size)
            assertEquals(1, lowerCase.places.size)
            assertEquals("1", lowerCase.places[0].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial isSearchActive is false`() {
        every { getPlacesUseCase() } returns flowOf(emptyList())
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        assertFalse(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun `onSearchActiveChanged true updates isSearchActive in uiState`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onSearchActiveChanged(true)
            val activeState = awaitItem()

            assertTrue(activeState.isSearchActive)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchActiveChanged false collapses search bar in uiState`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            awaitItem() // full list

            viewModel.onSearchActiveChanged(true)
            awaitItem() // active = true

            viewModel.onSearchActiveChanged(false)
            val collapsedState = awaitItem()

            assertFalse(collapsedState.isSearchActive)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchActiveChanged does not affect places list`() = runTest {
        every { getPlacesUseCase() } returns flowOf(samplePlaces)
        viewModel = PlacesListViewModel(getPlacesUseCase, deletePlaceUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            val fullList = awaitItem()
            val placesCount = fullList.places.size

            viewModel.onSearchActiveChanged(true)
            val activeState = awaitItem()

            assertEquals(placesCount, activeState.places.size)
            assertTrue(activeState.isSearchActive)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
