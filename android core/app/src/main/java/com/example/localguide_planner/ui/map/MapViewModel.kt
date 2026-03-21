package com.example.localguide_planner.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
) : ViewModel() {

    val uiState: StateFlow<MapUiState> = getPlacesUseCase()
        .map { places -> MapUiState(places = places, isLoading = false) }
        .catch { e -> emit(MapUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(isLoading = true),
        )
}
