package com.example.localguide_planner.data.local.db.place

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY createdAt DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: String): PlaceEntity?

    @Upsert
    suspend fun upsertPlace(place: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlaceById(id: String)

    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()
}
