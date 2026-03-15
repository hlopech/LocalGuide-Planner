package com.example.localguide_planner.ui.places.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun PlaceDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PlaceDetailUiEffect.NavigateBack -> onNavigateBack()
                is PlaceDetailUiEffect.NavigateToEdit -> onNavigateToEdit(effect.placeId)
            }
        }
    }

    PlaceDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDeleteConfirmed = { viewModel.onDeleteClicked() },
        onToggleFavorite = { viewModel.onToggleFavorite() },
        onEditClicked = { viewModel.onEditClicked() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceDetailContent(
    uiState: PlaceDetailUiState,
    onNavigateBack: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeName = (uiState as? PlaceDetailUiState.Success)?.place?.name.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            PlaceDetailTopBar(
                title = placeName,
                onNavigateBack = onNavigateBack,
                onEditClicked = onEditClicked,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                is PlaceDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is PlaceDetailUiState.Success -> {
                    PlaceDetailSuccessContent(
                        place = uiState.place,
                        onDeleteConfirmed = onDeleteConfirmed,
                        onToggleFavorite = onToggleFavorite,
                    )
                }

                is PlaceDetailUiState.Error -> {
                    val message = uiState.message.ifEmpty {
                        stringResource(R.string.place_detail_not_found)
                    }
                    PlaceDetailErrorContent(
                        message = message,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClicked) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.place_detail_edit),
                )
            }
        },
    )
}

@Composable
private fun PlaceDetailSuccessContent(
    place: Place,
    onDeleteConfirmed: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        PlaceDetailCategorySection(category = place.category)
        Spacer(modifier = Modifier.height(16.dp))
        PlaceDetailAddressSection(address = place.address)
        Spacer(modifier = Modifier.height(16.dp))
        PlaceDetailDescriptionSection(description = place.description)
        Spacer(modifier = Modifier.height(24.dp))
        PlaceDetailActionButtons(
            isFavorite = place.isFavorite,
            onToggleFavorite = onToggleFavorite,
            onDeleteClicked = { showDeleteDialog = true },
        )
    }
}

@Composable
private fun PlaceDetailCategorySection(
    category: PlaceCategory,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaceDetailSectionLabel(text = stringResource(R.string.place_detail_section_category))
        SuggestionChip(
            onClick = {},
            label = { Text(text = stringResource(category.toStringRes())) },
        )
    }
}

@Composable
private fun PlaceDetailAddressSection(
    address: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaceDetailSectionLabel(text = stringResource(R.string.place_detail_section_address))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun PlaceDetailDescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaceDetailSectionLabel(text = stringResource(R.string.place_detail_section_description))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PlaceDetailActionButtons(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) {
                    stringResource(R.string.place_detail_favorite_remove)
                } else {
                    stringResource(R.string.place_detail_favorite_add)
                },
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onDeleteClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(R.string.place_detail_delete),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun PlaceDetailSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun PlaceDetailErrorContent(
    message: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text(text = stringResource(R.string.action_go_back))
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.place_detail_delete_confirm_title)) },
        text = { Text(text = stringResource(R.string.place_detail_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Preview(showBackground = true, name = "PlaceDetailScreen — Success")
@Composable
fun PlaceDetailScreenSuccessPreview() {
    LocalGuidePlannerTheme {
        PlaceDetailContent(
            uiState = PlaceDetailUiState.Success(
                place = Place(
                    id = "1",
                    name = "Центральный парк",
                    description = "Красивый городской парк с фонтанами и аллеями для прогулок.",
                    category = PlaceCategory.PARK,
                    address = "ул. Ленина, 1",
                    latitude = 55.75,
                    longitude = 37.62,
                    isFavorite = true,
                    createdAt = System.currentTimeMillis(),
                ),
            ),
            onNavigateBack = {},
            onDeleteConfirmed = {},
            onToggleFavorite = {},
            onEditClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "PlaceDetailScreen — Loading")
@Composable
fun PlaceDetailScreenLoadingPreview() {
    LocalGuidePlannerTheme {
        PlaceDetailContent(
            uiState = PlaceDetailUiState.Loading,
            onNavigateBack = {},
            onDeleteConfirmed = {},
            onToggleFavorite = {},
            onEditClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "PlaceDetailScreen — Error")
@Composable
fun PlaceDetailScreenErrorPreview() {
    LocalGuidePlannerTheme {
        PlaceDetailContent(
            uiState = PlaceDetailUiState.Error(message = "Место не найдено"),
            onNavigateBack = {},
            onDeleteConfirmed = {},
            onToggleFavorite = {},
            onEditClicked = {},
        )
    }
}
