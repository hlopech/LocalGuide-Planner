package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetPlacesUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    operator fun invoke(): Flow<List<Place>> = repository.getPlaces()
}
