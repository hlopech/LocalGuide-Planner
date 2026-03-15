package com.example.localguide_planner.ui.places.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.usecase.place.DeletePlaceUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlaceByIdUseCase
import com.example.localguide_planner.domain.usecase.place.SavePlaceUseCase
import com.example.localguide_planner.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaceByIdUseCase: GetPlaceByIdUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val savePlaceUseCase: SavePlaceUseCase,
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle[Screen.PlaceDetail.ARG_PLACE_ID])

    private val _uiState = MutableStateFlow<PlaceDetailUiState>(PlaceDetailUiState.Loading)
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlaceDetailUiEffect>()
    val uiEffect: SharedFlow<PlaceDetailUiEffect> = _uiEffect.asSharedFlow()

    init {
        loadPlace()
    }

    private fun loadPlace() {
        viewModelScope.launch {
            val place = getPlaceByIdUseCase(placeId)
            _uiState.value = if (place != null) {
                PlaceDetailUiState.Success(place)
            } else {
                PlaceDetailUiState.Error()
            }
        }
    }

    fun onDeleteClicked() {
        viewModelScope.launch {
            deletePlaceUseCase(placeId)
            _uiEffect.emit(PlaceDetailUiEffect.NavigateBack)
        }
    }

    fun onToggleFavorite() {
        val current = (_uiState.value as? PlaceDetailUiState.Success)?.place ?: return
        viewModelScope.launch {
            savePlaceUseCase(current.copy(isFavorite = !current.isFavorite))
            loadPlace()
        }
    }

    fun onEditClicked() {
        viewModelScope.launch {
            _uiEffect.emit(PlaceDetailUiEffect.NavigateToEdit(placeId))
        }
    }
}
