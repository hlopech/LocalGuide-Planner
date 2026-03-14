package com.example.localguide_planner.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localguide_planner.domain.model.UserProfile
import com.example.localguide_planner.domain.usecase.profile.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_NAME_LENGTH = 50

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OnboardingUiEffect>()
    val uiEffect: SharedFlow<OnboardingUiEffect> = _uiEffect.asSharedFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onGetStartedClicked() {
        val name = _uiState.value.name.trim()
        when {
            name.isBlank() -> {
                _uiState.update { it.copy(nameError = NAME_ERROR_EMPTY) }
                return
            }
            name.length > MAX_NAME_LENGTH -> {
                _uiState.update { it.copy(nameError = NAME_ERROR_TOO_LONG) }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                saveUserProfileUseCase(UserProfile(name = name, isOnboardingCompleted = true))
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(OnboardingUiEffect.NavigateToMain)
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        const val NAME_ERROR_EMPTY = "name_error_empty"
        const val NAME_ERROR_TOO_LONG = "name_error_too_long"
    }
}
