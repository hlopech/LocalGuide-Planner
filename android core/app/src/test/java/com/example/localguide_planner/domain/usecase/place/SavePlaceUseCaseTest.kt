package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.repository.PlacesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePlaceUseCaseTest {

    private val repository: PlacesRepository = mockk()
    private val useCase = SavePlaceUseCase(repository)

    private fun buildPlace(
        id: String = "existing-id",
        createdAt: Long = 12345L
    ) = Place(
        id = id,
        name = "Test Place",
        description = "Description",
        category = PlaceCategory.RESTAURANT,
        address = "Test Address",
        latitude = null,
        longitude = null,
        isFavorite = false,
        createdAt = createdAt
    )

    @Test
    fun `invoke with blank id generates UUID and sets createdAt`() = runTest {
        val placeSlot = slot<Place>()
        coEvery { repository.savePlace(capture(placeSlot)) } returns Unit

        val newPlace = buildPlace(id = "", createdAt = 0L)
        useCase(newPlace)

        val saved = placeSlot.captured
        assertTrue("UUID must not be blank", saved.id.isNotBlank())
        assertTrue("Generated id should look like a UUID", saved.id.length == 36)
        assertTrue("createdAt must be positive", saved.createdAt > 0L)
    }

    @Test
    fun `invoke with blank id preserves all other fields`() = runTest {
        val placeSlot = slot<Place>()
        coEvery { repository.savePlace(capture(placeSlot)) } returns Unit

        val newPlace = buildPlace(id = "")
        useCase(newPlace)

        val saved = placeSlot.captured
        assertEquals(newPlace.name, saved.name)
        assertEquals(newPlace.description, saved.description)
        assertEquals(newPlace.category, saved.category)
        assertEquals(newPlace.address, saved.address)
        assertEquals(newPlace.isFavorite, saved.isFavorite)
    }

    @Test
    fun `invoke with existing id preserves id and createdAt`() = runTest {
        val placeSlot = slot<Place>()
        coEvery { repository.savePlace(capture(placeSlot)) } returns Unit

        val existingPlace = buildPlace(id = "existing-id", createdAt = 12345L)
        useCase(existingPlace)

        val saved = placeSlot.captured
        assertEquals("existing-id", saved.id)
        assertEquals(12345L, saved.createdAt)
    }

    @Test
    fun `invoke calls repository savePlace exactly once`() = runTest {
        coEvery { repository.savePlace(any()) } returns Unit

        val place = buildPlace(id = "some-id")
        useCase(place)

        coVerify(exactly = 1) { repository.savePlace(any()) }
    }

    @Test
    fun `invoke with whitespace id generates new UUID`() = runTest {
        val placeSlot = slot<Place>()
        coEvery { repository.savePlace(capture(placeSlot)) } returns Unit

        val newPlace = buildPlace(id = "   ", createdAt = 0L)
        useCase(newPlace)

        val saved = placeSlot.captured
        assertTrue("UUID must not be blank", saved.id.isNotBlank())
        assertNotEquals("   ", saved.id)
    }
}
