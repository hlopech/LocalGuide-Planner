package com.example.localguide_planner.ui.map

import com.example.localguide_planner.domain.model.Place

data class MapUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
