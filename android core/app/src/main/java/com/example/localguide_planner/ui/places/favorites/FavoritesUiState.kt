package com.example.localguide_planner.ui.places.favorites

import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory

data class FavoritesUiState(
    val places: List<Place> = emptyList(),
    val selectedCategory: PlaceCategory? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
