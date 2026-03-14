package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject

class DeletePlaceUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(id: String) = repository.deletePlace(id)
}
