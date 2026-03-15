package com.example.localguide_planner.ui.places.addedit

import com.example.localguide_planner.domain.model.PlaceCategory

data class AddEditPlaceUiState(
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val category: PlaceCategory = PlaceCategory.OTHER,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
)

sealed class AddEditPlaceUiEffect {
    data object NavigateBack : AddEditPlaceUiEffect()
}
