package com.example.localguide_planner.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is OnboardingUiEffect.NavigateToMain -> onOnboardingCompleted()
            }
        }
    }

    OnboardingContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onGetStartedClicked = viewModel::onGetStartedClicked,
    )
}

@Composable
fun OnboardingContent(
    uiState: OnboardingUiState,
    onNameChanged: (String) -> Unit,
    onGetStartedClicked: () -> Unit,
) {
    val nameErrorEmpty = stringResource(R.string.onboarding_name_error_empty)
    val nameErrorTooLong = stringResource(R.string.onboarding_name_error_too_long)

    val resolvedError = when (uiState.nameError) {
        OnboardingViewModel.NAME_ERROR_EMPTY -> nameErrorEmpty
        OnboardingViewModel.NAME_ERROR_TOO_LONG -> nameErrorTooLong
        else -> uiState.nameError
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                isError = resolvedError != null,
                supportingText = resolvedError?.let { error ->
                    { Text(error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Button(
                    onClick = onGetStartedClicked,
                    enabled = uiState.name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_button_continue))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingContentPreview() {
    LocalGuidePlannerTheme {
        OnboardingContent(
            uiState = OnboardingUiState(name = "Алексей"),
            onNameChanged = {},
            onGetStartedClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding - empty state")
@Composable
fun OnboardingContentEmptyPreview() {
    LocalGuidePlannerTheme {
        OnboardingContent(
            uiState = OnboardingUiState(),
            onNameChanged = {},
            onGetStartedClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding - error state")
@Composable
fun OnboardingContentErrorPreview() {
    LocalGuidePlannerTheme {
        OnboardingContent(
            uiState = OnboardingUiState(nameError = OnboardingViewModel.NAME_ERROR_EMPTY),
            onNameChanged = {},
            onGetStartedClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "Onboarding - loading state")
@Composable
fun OnboardingContentLoadingPreview() {
    LocalGuidePlannerTheme {
        OnboardingContent(
            uiState = OnboardingUiState(name = "Алексей", isLoading = true),
            onNameChanged = {},
            onGetStartedClicked = {},
        )
    }
}
