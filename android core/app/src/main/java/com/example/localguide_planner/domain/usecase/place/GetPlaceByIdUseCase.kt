package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject

class GetPlaceByIdUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(id: String): Place? = repository.getPlaceById(id)
}
