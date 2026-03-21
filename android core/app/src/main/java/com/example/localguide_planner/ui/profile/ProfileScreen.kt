package com.example.localguide_planner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.places.common.LocalGuideTextField
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onSave = viewModel::saveProfile,
        onShowResetDialog = viewModel::showResetDialog,
        onDismissResetDialog = viewModel::dismissResetDialog,
        onConfirmReset = viewModel::resetAllData,
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onShowResetDialog: () -> Unit,
    onDismissResetDialog: () -> Unit,
    onConfirmReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileHeroSection(userName = uiState.userName)
        ProfileStatsRow(
            totalCount = uiState.totalPlacesCount,
            favoritesCount = uiState.favoritePlacesCount,
            topCategory = uiState.topCategory,
        )
        ProfileEditSection(
            editedName = uiState.editedName,
            savedName = uiState.userName,
            isSaving = uiState.isSaving,
            onNameChanged = onNameChanged,
            onSave = onSave,
        )
        ProfileResetButton(onClick = onShowResetDialog)
    }

    if (uiState.showResetConfirmDialog) {
        ResetConfirmDialog(
            onConfirm = onConfirmReset,
            onDismiss = onDismissResetDialog,
        )
    }
}

@Composable
private fun ProfileHeroSection(userName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalGuideDesignTokens.heroSectionHeight)
            .background(
                brush = Brush.horizontalGradient(LocalGuideDesignTokens.heroGradientColors),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = userName.ifEmpty { "—" },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ProfileStatsRow(
    totalCount: Int,
    favoritesCount: Int,
    topCategory: PlaceCategory?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            value = totalCount.toString(),
            label = stringResource(R.string.profile_stat_total_places),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = favoritesCount.toString(),
            label = stringResource(R.string.profile_stat_favorites),
            modifier = Modifier.weight(1f),
        )
        val topCategoryStr = topCategory?.let { stringResource(it.toStringRes()) }
            ?: stringResource(R.string.profile_stat_top_category_none)
        StatCard(
            value = topCategoryStr,
            label = stringResource(R.string.profile_stat_top_category),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileEditSection(
    editedName: String,
    savedName: String,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        LocalGuideTextField(
            value = editedName,
            onValueChange = onNameChanged,
            label = stringResource(R.string.profile_edit_name_label),
            leadingIcon = Icons.Rounded.Person,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSave,
            enabled = editedName.isNotBlank() && editedName != savedName && !isSaving,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.profile_save_button))
            }
        }
    }
}

@Composable
private fun ProfileResetButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(stringResource(R.string.profile_reset_button))
    }
}

@Composable
private fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_reset_dialog_title)) },
        text = { Text(stringResource(R.string.profile_reset_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Preview(showBackground = true, name = "ProfileScreen — с данными")
@Composable
private fun ProfileContentWithDataPreview() {
    LocalGuidePlannerTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "Алексей",
                editedName = "Алексей",
                totalPlacesCount = 5,
                favoritePlacesCount = 2,
                topCategory = PlaceCategory.CAFE,
                isLoading = false,
            ),
            onNameChanged = {},
            onSave = {},
            onShowResetDialog = {},
            onDismissResetDialog = {},
            onConfirmReset = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileScreen — пустой")
@Composable
private fun ProfileContentEmptyPreview() {
    LocalGuidePlannerTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "",
                editedName = "",
                totalPlacesCount = 0,
                favoritePlacesCount = 0,
                topCategory = null,
                isLoading = false,
            ),
            onNameChanged = {},
            onSave = {},
            onShowResetDialog = {},
            onDismissResetDialog = {},
            onConfirmReset = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileScreen — диалог сброса")
@Composable
private fun ProfileContentResetDialogPreview() {
    LocalGuidePlannerTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "Алексей",
                editedName = "Алексей",
                totalPlacesCount = 3,
                favoritePlacesCount = 1,
                topCategory = null,
                isLoading = false,
                showResetConfirmDialog = true,
            ),
            onNameChanged = {},
            onSave = {},
            onShowResetDialog = {},
            onDismissResetDialog = {},
            onConfirmReset = {},
        )
    }
}
