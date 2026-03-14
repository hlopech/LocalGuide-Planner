package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.repository.PlacesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPlacesUseCaseTest {

    private val repository: PlacesRepository = mockk()
    private val useCase = GetPlacesUseCase(repository)

    private fun buildPlace(id: String = "1") = Place(
        id = id,
        name = "Test Place",
        description = "Description",
        category = PlaceCategory.PARK,
        address = "Test Address",
        latitude = null,
        longitude = null,
        isFavorite = false,
        createdAt = 0L
    )

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val places = listOf(buildPlace("1"), buildPlace("2"))
        every { repository.getPlaces() } returns flowOf(places)

        val result = useCase().first()

        assertEquals(places, result)
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        every { repository.getPlaces() } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(emptyList<Place>(), result)
    }
}
