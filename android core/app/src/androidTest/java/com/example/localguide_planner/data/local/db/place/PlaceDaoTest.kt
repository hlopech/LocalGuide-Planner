package com.example.localguide_planner.data.local.db.place

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.localguide_planner.data.local.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PlaceDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.placeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllPlaces_emptyDatabase_returnsEmptyList() = runBlocking {
        val result = dao.getAllPlaces().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun insertPlace_and_getAll_returnsInsertedPlace() = runBlocking {
        val place = buildPlaceEntity(id = "1", name = "Парк Горького")
        dao.upsertPlace(place)

        val result = dao.getAllPlaces().first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Парк Горького", result[0].name)
    }

    @Test
    fun insertMultiplePlaces_getAll_returnsAll() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "1", name = "Место А"))
        dao.upsertPlace(buildPlaceEntity(id = "2", name = "Место Б"))
        dao.upsertPlace(buildPlaceEntity(id = "3", name = "Место В"))

        val result = dao.getAllPlaces().first()

        assertEquals(3, result.size)
    }

    @Test
    fun upsertPlace_conflictingId_replacesExistingRecord() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "1", name = "Старое название"))
        dao.upsertPlace(buildPlaceEntity(id = "1", name = "Новое название"))

        val result = dao.getAllPlaces().first()

        assertEquals(1, result.size)
        assertEquals("Новое название", result[0].name)
    }

    @Test
    fun getPlaceById_existingId_returnsPlace() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "42", name = "Тестовое место"))

        val result = dao.getPlaceById("42")

        assertNotNull(result)
        assertEquals("42", result?.id)
    }

    @Test
    fun getPlaceById_nonExistingId_returnsNull() = runBlocking {
        val result = dao.getPlaceById("not-exist")

        assertNull(result)
    }

    @Test
    fun deletePlace_getAll_doesNotContainDeleted() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "1", name = "Место первое"))
        dao.upsertPlace(buildPlaceEntity(id = "2", name = "Место второе"))

        dao.deletePlaceById("1")

        val result = dao.getAllPlaces().first()
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun updatePlace_getById_returnsUpdated() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "10", name = "Исходное имя"))
        dao.upsertPlace(buildPlaceEntity(id = "10", name = "Обновлённое имя"))

        val result = dao.getPlaceById("10")

        assertNotNull(result)
        assertEquals("Обновлённое имя", result?.name)
    }

    @Test
    fun getAllPlaces_sortedByCreatedAtDesc() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "a", name = "Старое", createdAt = 100L))
        dao.upsertPlace(buildPlaceEntity(id = "b", name = "Новейшее", createdAt = 300L))
        dao.upsertPlace(buildPlaceEntity(id = "c", name = "Среднее", createdAt = 200L))

        val result = dao.getAllPlaces().first()

        assertEquals(3, result.size)
        assertEquals(300L, result[0].createdAt)
        assertEquals(200L, result[1].createdAt)
        assertEquals(100L, result[2].createdAt)
    }

    @Test
    fun upsertPlace_toggleFavorite_isFavoriteFieldUpdated() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "fav1", name = "Избранное место", isFavorite = false))
        dao.upsertPlace(buildPlaceEntity(id = "fav1", name = "Избранное место", isFavorite = true))

        val result = dao.getPlaceById("fav1")

        assertNotNull(result)
        assertTrue(result!!.isFavorite)
    }

    @Test
    fun upsertPlace_unfavoritePlace_isFavoriteFieldUpdated() = runBlocking {
        dao.upsertPlace(buildPlaceEntity(id = "fav2", name = "Было избранным", isFavorite = true))
        dao.upsertPlace(buildPlaceEntity(id = "fav2", name = "Было избранным", isFavorite = false))

        val result = dao.getPlaceById("fav2")

        assertNotNull(result)
        assertFalse(result!!.isFavorite)
    }

    private fun buildPlaceEntity(
        id: String,
        name: String,
        createdAt: Long = System.currentTimeMillis(),
        isFavorite: Boolean = false,
    ): PlaceEntity = PlaceEntity(
        id = id,
        name = name,
        description = "Тестовое описание",
        category = "PARK",
        address = "ул. Тестовая, 1",
        latitude = null,
        longitude = null,
        isFavorite = isFavorite,
        createdAt = createdAt,
    )
}
