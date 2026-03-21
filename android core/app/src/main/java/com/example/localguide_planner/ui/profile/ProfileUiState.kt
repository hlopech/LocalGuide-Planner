package com.example.localguide_planner.ui.profile

import com.example.localguide_planner.domain.model.PlaceCategory

data class ProfileUiState(
    val userName: String = "",
    val editedName: String = "",
    val totalPlacesCount: Int = 0,
    val favoritePlacesCount: Int = 0,
    val topCategory: PlaceCategory? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val errorMessage: String? = null,
)
