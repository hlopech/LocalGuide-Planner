package com.example.localguide_planner.ui.places.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.DeletePlaceUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlacesListViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)

    val uiState: StateFlow<PlacesListUiState> = combine(
        getPlacesUseCase(),
        _searchQuery,
        _selectedCategory,
    ) { places, query, category ->
        val filtered = places.filter { place ->
            val matchesQuery = query.isBlank() ||
                place.name.contains(query, ignoreCase = true) ||
                place.address.contains(query, ignoreCase = true)
            val matchesCategory = category == null || place.category == category
            matchesQuery && matchesCategory
        }
        PlacesListUiState(
            places = filtered,
            searchQuery = query,
            selectedCategory = category,
            isLoading = false,
        )
    }
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: PlaceCategory?) {
        _selectedCategory.value = category
    }
}
