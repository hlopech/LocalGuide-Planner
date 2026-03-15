package com.example.localguide_planner.ui.places.list

import com.example.localguide_planner.domain.model.Place

data class PlacesListUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
