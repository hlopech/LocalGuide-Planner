package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject

class DeleteAllPlacesUseCase @Inject constructor(
    private val placesRepository: PlacesRepository,
) {
    suspend operator fun invoke() = placesRepository.deleteAllPlaces()
}
