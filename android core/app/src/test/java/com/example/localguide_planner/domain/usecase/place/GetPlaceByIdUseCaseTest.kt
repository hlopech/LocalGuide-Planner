package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.repository.PlacesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetPlaceByIdUseCaseTest {

    private val repository: PlacesRepository = mockk()
    private val useCase = GetPlaceByIdUseCase(repository)

    private fun buildPlace(id: String = "test-id") = Place(
        id = id,
        name = "Test Place",
        description = "Description",
        category = PlaceCategory.MUSEUM,
        address = "Test Address",
        latitude = 55.0,
        longitude = 37.0,
        isFavorite = false,
        createdAt = 1000L
    )

    @Test
    fun `invoke returns place when found`() = runTest {
        val place = buildPlace("test-id")
        coEvery { repository.getPlaceById("test-id") } returns place

        val result = useCase("test-id")

        assertEquals(place, result)
    }

    @Test
    fun `invoke returns null when place not found`() = runTest {
        coEvery { repository.getPlaceById("non-existent-id") } returns null

        val result = useCase("non-existent-id")

        assertNull(result)
    }
}
