package com.example.localguide_planner.data.local.db.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaceMapperTest {

    private fun buildPlace(
        id: String = "place-1",
        latitude: Double? = 55.7558,
        longitude: Double? = 37.6173
    ) = Place(
        id = id,
        name = "Red Square",
        description = "Historic square in Moscow",
        category = PlaceCategory.LANDMARK,
        address = "Red Square, Moscow",
        latitude = latitude,
        longitude = longitude,
        isFavorite = true,
        createdAt = 1_700_000_000L
    )

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain = buildPlace()

        val entity = PlaceMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.category.name, entity.category)
        assertEquals(domain.address, entity.address)
        assertEquals(domain.latitude, entity.latitude)
        assertEquals(domain.longitude, entity.longitude)
        assertEquals(domain.isFavorite, entity.isFavorite)
        assertEquals(domain.createdAt, entity.createdAt)
    }

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = PlaceEntity(
            id = "place-2",
            name = "Gorky Park",
            description = "Famous park in Moscow",
            category = PlaceCategory.PARK.name,
            address = "Gorky Park, Moscow",
            latitude = 55.7298,
            longitude = 37.6012,
            isFavorite = false,
            createdAt = 1_600_000_000L
        )

        val domain = PlaceMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.description, domain.description)
        assertEquals(PlaceCategory.PARK, domain.category)
        assertEquals(entity.address, domain.address)
        assertEquals(entity.latitude, domain.latitude)
        assertEquals(entity.longitude, domain.longitude)
        assertEquals(entity.isFavorite, domain.isFavorite)
        assertEquals(entity.createdAt, domain.createdAt)
    }

    @Test
    fun `roundtrip domain to entity to domain preserves all fields`() {
        val original = buildPlace()

        val roundtrip = PlaceMapper.toDomain(PlaceMapper.toEntity(original))

        assertEquals(original, roundtrip)
    }

    @Test
    fun `roundtrip with null coordinates preserves null values`() {
        val original = buildPlace(latitude = null, longitude = null)

        val roundtrip = PlaceMapper.toDomain(PlaceMapper.toEntity(original))

        assertNull(roundtrip.latitude)
        assertNull(roundtrip.longitude)
        assertEquals(original, roundtrip)
    }

    @Test
    fun `toEntity stores category as enum name string`() {
        PlaceCategory.entries.forEach { category ->
            val domain = buildPlace().copy(category = category)
            val entity = PlaceMapper.toEntity(domain)
            assertEquals(category.name, entity.category)
        }
    }

    @Test
    fun `toDomain restores all PlaceCategory enum values`() {
        PlaceCategory.entries.forEach { category ->
            val entity = PlaceEntity(
                id = "id",
                name = "name",
                description = "desc",
                category = category.name,
                address = "addr",
                latitude = null,
                longitude = null,
                isFavorite = false,
                createdAt = 0L
            )
            val domain = PlaceMapper.toDomain(entity)
            assertEquals(category, domain.category)
        }
    }
}
