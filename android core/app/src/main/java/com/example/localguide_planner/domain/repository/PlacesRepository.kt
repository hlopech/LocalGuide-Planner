package com.example.localguide_planner.domain.repository

import com.example.localguide_planner.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getPlaces(): Flow<List<Place>>
    suspend fun getPlaceById(id: String): Place?
    suspend fun savePlace(place: Place)
    suspend fun deletePlace(id: String)
}
