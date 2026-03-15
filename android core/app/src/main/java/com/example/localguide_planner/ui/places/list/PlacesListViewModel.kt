package com.example.localguide_planner.ui.places.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.usecase.place.DeletePlaceUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlacesListViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
) : ViewModel() {

    val uiState: StateFlow<PlacesListUiState> = getPlacesUseCase()
        .map { places -> PlacesListUiState(places = places, isLoading = false) }
        .catch { e -> emit(PlacesListUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlacesListUiState(isLoading = true),
        )

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            deletePlaceUseCase(placeId)
        }
    }
}
