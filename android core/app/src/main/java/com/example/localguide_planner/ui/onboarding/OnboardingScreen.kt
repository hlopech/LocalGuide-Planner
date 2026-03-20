package com.example.localguide_planner.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens
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

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OnboardingHeroSection(visible = visible)
            OnboardingFormSection(
                uiState = uiState,
                visible = visible,
                resolvedError = resolvedError,
                onNameChanged = onNameChanged,
                onGetStartedClicked = onGetStartedClicked,
            )
        }
    }
}

@Composable
private fun OnboardingHeroSection(visible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
            .background(
                brush = Brush.verticalGradient(colors = LocalGuideDesignTokens.heroGradientColors),
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 2 },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 2 },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private fun formItemEnterTransition(delayMillis: Int): EnterTransition =
    fadeIn(tween(300, delayMillis = delayMillis)) +
        slideInVertically(tween(300, delayMillis = delayMillis)) { it / 2 }

@Composable
private fun OnboardingFormSection(
    uiState: OnboardingUiState,
    visible: Boolean,
    resolvedError: String?,
    onNameChanged: (String) -> Unit,
    onGetStartedClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 32.dp, end = 32.dp),
    ) {
        AnimatedVisibility(visible = visible, enter = formItemEnterTransition(150)) {
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(visible = visible, enter = formItemEnterTransition(250)) {
            OnboardingTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                error = resolvedError,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(visible = visible, enter = formItemEnterTransition(350)) {
            OnboardingButton(
                isLoading = uiState.isLoading,
                enabled = uiState.name.isNotBlank(),
                onClick = onGetStartedClicked,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.onboarding_name_label)) },
        isError = error != null,
        supportingText = error?.let { msg -> { Text(msg) } },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
    )
}

@Composable
private fun OnboardingButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "button_scale",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            },
    ) {
        Crossfade(targetState = isLoading, label = "button_crossfade") { loading ->
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                Text(stringResource(R.string.onboarding_button_continue))
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
