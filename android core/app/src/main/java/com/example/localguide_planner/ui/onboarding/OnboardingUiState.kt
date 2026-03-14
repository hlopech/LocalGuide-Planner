package com.example.localguide_planner.ui.onboarding

data class OnboardingUiState(
    val name: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
)

sealed class OnboardingUiEffect {
    data object NavigateToMain : OnboardingUiEffect()
}
