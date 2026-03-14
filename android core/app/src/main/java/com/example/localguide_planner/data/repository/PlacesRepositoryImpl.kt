package com.example.localguide_planner.data.repository

import com.example.localguide_planner.data.local.db.place.PlaceDao
import com.example.localguide_planner.data.local.db.place.PlaceMapper
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlacesRepositoryImpl @Inject constructor(
    private val dao: PlaceDao
) : PlacesRepository {

    override fun getPlaces(): Flow<List<Place>> =
        dao.getAllPlaces().map { entities -> entities.map(PlaceMapper::toDomain) }

    override suspend fun getPlaceById(id: String): Place? =
        dao.getPlaceById(id)?.let(PlaceMapper::toDomain)

    override suspend fun savePlace(place: Place) =
        dao.upsertPlace(PlaceMapper.toEntity(place))

    override suspend fun deletePlace(id: String) =
        dao.deletePlaceById(id)
}
