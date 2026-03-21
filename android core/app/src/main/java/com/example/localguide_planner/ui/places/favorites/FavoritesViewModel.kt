package com.example.localguide_planner.ui.places.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)

    val uiState: StateFlow<FavoritesUiState> = combine(
        getPlacesUseCase(),
        _selectedCategory,
    ) { places, category ->
        val favorites = places.filter { it.isFavorite }
        val filtered = if (category == null) favorites
        else favorites.filter { it.category == category }
        FavoritesUiState(
            places = filtered,
            selectedCategory = category,
            isLoading = false,
        )
    }
        .catch { e -> emit(FavoritesUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(isLoading = true),
        )

    fun onCategorySelected(category: PlaceCategory?) {
        _selectedCategory.value = category
    }
}
