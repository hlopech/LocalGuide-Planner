package com.example.localguide_planner.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.usecase.place.DeleteAllPlacesUseCase
import com.example.localguide_planner.domain.usecase.place.GetPlacesUseCase
import com.example.localguide_planner.domain.usecase.profile.GetUserProfileUseCase
import com.example.localguide_planner.domain.usecase.profile.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val getPlacesUseCase: GetPlacesUseCase,
    private val deleteAllPlacesUseCase: DeleteAllPlacesUseCase,
) : ViewModel() {

    private val _editedName = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)
    private val _showResetDialog = MutableStateFlow(false)

    private val _latestProfile = MutableStateFlow<UserProfile?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        getUserProfileUseCase(),
        getPlacesUseCase(),
        _editedName,
        _isSaving,
        _showResetDialog,
    ) { profile, places, editedName, isSaving, showDialog ->
        _latestProfile.value = profile
        val topCategory = places
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.key
        ProfileUiState(
            userName = profile.name,
            editedName = editedName.ifEmpty { profile.name },
            totalPlacesCount = places.size,
            favoritePlacesCount = places.count { it.isFavorite },
            topCategory = topCategory,
            isLoading = false,
            isSaving = isSaving,
            showResetConfirmDialog = showDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(isLoading = true),
    )

    fun onNameChanged(name: String) {
        _editedName.value = name
    }

    fun saveProfile() {
        viewModelScope.launch {
            _isSaving.value = true
            val currentProfile = _latestProfile.value
            saveUserProfileUseCase(
                UserProfile(
                    name = _editedName.value,
                    isOnboardingCompleted = currentProfile?.isOnboardingCompleted ?: true,
                ),
            )
            _isSaving.value = false
        }
    }

    fun showResetDialog() {
        _showResetDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun resetAllData() {
        viewModelScope.launch {
            deleteAllPlacesUseCase()
            _showResetDialog.value = false
        }
    }
}
