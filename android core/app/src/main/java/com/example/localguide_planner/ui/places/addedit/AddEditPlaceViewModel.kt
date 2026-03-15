package com.example.localguide_planner.ui.places.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
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
class AddEditPlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaceByIdUseCase: GetPlaceByIdUseCase,
    private val savePlaceUseCase: SavePlaceUseCase,
) : ViewModel() {

    private val placeId: String? = savedStateHandle[Screen.EditPlace.ARG_PLACE_ID]

    private val _uiState = MutableStateFlow(AddEditPlaceUiState())
    val uiState: StateFlow<AddEditPlaceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AddEditPlaceUiEffect>()
    val uiEffect: SharedFlow<AddEditPlaceUiEffect> = _uiEffect.asSharedFlow()

    // Stores original place fields that should not be overwritten on edit
    private var originalId: String = ""
    private var originalCreatedAt: Long = 0L
    private var originalIsFavorite: Boolean = false
    private var originalLatitude: Double? = null
    private var originalLongitude: Double? = null

    init {
        if (placeId != null) {
            loadPlace(placeId)
        }
    }

    private fun loadPlace(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val place = getPlaceByIdUseCase(id)
            if (place == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _uiEffect.emit(AddEditPlaceUiEffect.NavigateBack)
                return@launch
            }
            originalId = place.id
            originalCreatedAt = place.createdAt
            originalIsFavorite = place.isFavorite
            originalLatitude = place.latitude
            originalLongitude = place.longitude
            _uiState.value = _uiState.value.copy(
                name = place.name,
                address = place.address,
                description = place.description,
                category = place.category,
                isLoading = false,
                isEditMode = true,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onCategoryChange(value: PlaceCategory) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = NAME_ERROR_KEY)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val place = buildPlace(state)
            savePlaceUseCase(place)
            _uiState.value = _uiState.value.copy(isSaving = false)
            _uiEffect.emit(AddEditPlaceUiEffect.NavigateBack)
        }
    }

    fun onNavigateBackClicked() {
        viewModelScope.launch {
            _uiEffect.emit(AddEditPlaceUiEffect.NavigateBack)
        }
    }

    private fun buildPlace(state: AddEditPlaceUiState): Place = if (state.isEditMode) {
        Place(
            id = originalId,
            name = state.name,
            address = state.address,
            description = state.description,
            category = state.category,
            latitude = originalLatitude,
            longitude = originalLongitude,
            isFavorite = originalIsFavorite,
            createdAt = originalCreatedAt,
        )
    } else {
        Place(
            id = "",
            name = state.name,
            address = state.address,
            description = state.description,
            category = state.category,
            latitude = null,
            longitude = null,
            isFavorite = false,
            createdAt = 0L,
        )
    }

    companion object {
        const val NAME_ERROR_KEY = "name_error"
    }
}
