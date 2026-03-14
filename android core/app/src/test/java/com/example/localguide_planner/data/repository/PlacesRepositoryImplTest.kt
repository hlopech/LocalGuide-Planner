package com.example.localguide_planner.data.repository

import com.example.localguide_planner.data.local.db.place.PlaceDao
import com.example.localguide_planner.data.local.db.place.PlaceEntity
import com.example.localguide_planner.data.local.db.place.PlaceMapper
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlacesRepositoryImplTest {

    private val dao: PlaceDao = mockk(relaxed = true)
    private val repository = PlacesRepositoryImpl(dao)

    private fun buildEntity(id: String = "1") = PlaceEntity(
        id = id,
        name = "Test Place",
        description = "Description",
        category = PlaceCategory.CAFE.name,
        address = "Test Address",
        latitude = 55.0,
        longitude = 37.0,
        isFavorite = false,
        createdAt = 1_000L
    )

    private fun buildDomain(id: String = "1") = Place(
        id = id,
        name = "Test Place",
        description = "Description",
        category = PlaceCategory.CAFE,
        address = "Test Address",
        latitude = 55.0,
        longitude = 37.0,
        isFavorite = false,
        createdAt = 1_000L
    )

    @Test
    fun `getPlaces returns mapped domain list from dao`() = runTest {
        val entities = listOf(buildEntity("1"), buildEntity("2"))
        every { dao.getAllPlaces() } returns flowOf(entities)

        val result = repository.getPlaces().first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
        assertEquals(PlaceCategory.CAFE, result[0].category)
    }

    @Test
    fun `getPlaces returns empty list when dao is empty`() = runTest {
        every { dao.getAllPlaces() } returns flowOf(emptyList())

        val result = repository.getPlaces().first()

        assertEquals(emptyList<Place>(), result)
    }

    @Test
    fun `getPlaceById returns mapped domain when entity found`() = runTest {
        coEvery { dao.getPlaceById("1") } returns buildEntity("1")

        val result = repository.getPlaceById("1")

        assertEquals("1", result?.id)
        assertEquals(PlaceCategory.CAFE, result?.category)
    }

    @Test
    fun `getPlaceById returns null when entity not found`() = runTest {
        coEvery { dao.getPlaceById("99") } returns null

        val result = repository.getPlaceById("99")

        assertNull(result)
    }

    @Test
    fun `savePlace calls dao upsertPlace with correct entity`() = runTest {
        val domain = buildDomain("42")
        val entitySlot = slot<PlaceEntity>()
        coEvery { dao.upsertPlace(capture(entitySlot)) } returns Unit

        repository.savePlace(domain)

        coVerify(exactly = 1) { dao.upsertPlace(any()) }
        assertEquals("42", entitySlot.captured.id)
        assertEquals(PlaceCategory.CAFE.name, entitySlot.captured.category)
    }

    @Test
    fun `deletePlace calls dao deletePlaceById with correct id`() = runTest {
        repository.deletePlace("55")

        coVerify(exactly = 1) { dao.deletePlaceById("55") }
    }

    @Test
    fun `savePlace then getPlaceById returns updated data`() = runTest {
        val domain = buildDomain("7")
        val expectedEntity = PlaceMapper.toEntity(domain)
        coEvery { dao.upsertPlace(any()) } returns Unit
        coEvery { dao.getPlaceById("7") } returns expectedEntity

        repository.savePlace(domain)
        val result = repository.getPlaceById("7")

        assertEquals(domain, result)
    }
}
