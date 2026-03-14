package com.example.localguide_planner.domain.usecase.place

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.repository.PlacesRepository
import javax.inject.Inject
import java.util.UUID

class SavePlaceUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(place: Place) {
        val placeToSave = if (place.id.isBlank()) {
            place.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis()
            )
        } else {
            place
        }
        repository.savePlace(placeToSave)
    }
}
