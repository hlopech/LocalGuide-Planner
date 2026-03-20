package com.example.localguide_planner.ui.places.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.places.common.toAccentColor
import com.example.localguide_planner.ui.places.common.toIcon
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme
import kotlinx.coroutines.delay

private val FavoriteRed = Color(0xFFE53935)

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val placeName = (uiState as? PlaceDetailUiState.Success)?.place?.name.orEmpty()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(placeName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClicked) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.place_detail_edit),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        Crossfade(
            targetState = uiState,
            modifier = Modifier.padding(innerPadding),
        ) { state ->
            when (state) {
                is PlaceDetailUiState.Loading -> PlaceDetailLoadingContent()
                is PlaceDetailUiState.Error -> PlaceDetailErrorContent(
                    message = state.message.ifEmpty {
                        stringResource(R.string.place_detail_not_found)
                    },
                    onRetry = onNavigateBack,
                    onNavigateBack = onNavigateBack,
                )
                is PlaceDetailUiState.Success -> PlaceDetailSuccessContent(
                    place = state.place,
                    onDeleteConfirmed = onDeleteConfirmed,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PlaceDetailErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.place_detail_error_retry))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.action_go_back))
        }
    }
}

@Composable
private fun PlaceDetailSuccessContent(
    place: Place,
    onDeleteConfirmed: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PlaceDetailHeroSection(place = place)
        PlaceDetailSectionsColumn(
            place = place,
            onDeleteConfirmed = onDeleteConfirmed,
            onToggleFavorite = onToggleFavorite,
        )
    }
}

@Composable
private fun PlaceDetailHeroSection(place: Place, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        place.category.toAccentColor().copy(alpha = 0.7f),
                        place.category.toAccentColor(),
                    ),
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            )
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + scaleIn(tween(400)),
            ) {
                Icon(
                    imageVector = place.category.toIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White,
                )
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 100)),
            ) {
                HeroNameRow(name = place.name, isFavorite = place.isFavorite)
            }
        }
    }
}

@Composable
private fun HeroNameRow(name: String, isFavorite: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (isFavorite) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = FavoriteRed,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PlaceDetailSectionsColumn(
    place: Place,
    onDeleteConfirmed: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = { showDeleteDialog = false; onDeleteConfirmed() },
            onDismiss = { showDeleteDialog = false },
        )
    }

    var sectionIndex = 0
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        AnimatedDetailSection(
            index = sectionIndex++,
            icon = Icons.Rounded.LocationOn,
            label = stringResource(R.string.place_detail_section_address),
            value = place.address,
        )
        AnimatedDetailSection(
            index = sectionIndex++,
            icon = place.category.toIcon(),
            label = stringResource(R.string.place_detail_section_category),
            value = stringResource(place.category.toStringRes()),
        )
        if (place.description.isNotEmpty()) {
            AnimatedDetailSection(
                index = sectionIndex++,
                icon = Icons.Rounded.Description,
                label = stringResource(R.string.place_detail_section_description),
                value = place.description,
            )
        }
        val dateStr = remember(place.createdAt) {
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(place.createdAt))
        }
        AnimatedDetailSection(
            index = sectionIndex++,
            icon = Icons.Rounded.CalendarToday,
            label = stringResource(R.string.place_detail_label_added),
            value = dateStr,
        )
        AnimatedFavoriteButton(
            index = sectionIndex++,
            isFavorite = place.isFavorite,
            onClick = onToggleFavorite,
        )
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.place_detail_delete),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun AnimatedFavoriteButton(
    index: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(minOf(index * 80L, 400L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 },
        modifier = modifier,
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isFavorite) {
                    stringResource(R.string.place_detail_action_remove_favorite)
                } else {
                    stringResource(R.string.place_detail_action_add_favorite)
                },
            )
        }
    }
}

@Composable
private fun AnimatedDetailSection(
    index: Int,
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(minOf(index * 80L, 400L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 },
        modifier = modifier,
    ) {
        DetailSection(icon = icon, label = label, value = value)
    }
}

@Composable
private fun DetailSection(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
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
