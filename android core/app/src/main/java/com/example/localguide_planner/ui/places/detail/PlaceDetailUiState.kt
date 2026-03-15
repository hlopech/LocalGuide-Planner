package com.example.localguide_planner.ui.places.detail

import com.example.localguide_planner.domain.model.Place

sealed class PlaceDetailUiState {
    data object Loading : PlaceDetailUiState()
    data class Success(val place: Place) : PlaceDetailUiState()
    data class Error(val message: String = "") : PlaceDetailUiState()
}

sealed class PlaceDetailUiEffect {
    data object NavigateBack : PlaceDetailUiEffect()
    data class NavigateToEdit(val placeId: String) : PlaceDetailUiEffect()
}
