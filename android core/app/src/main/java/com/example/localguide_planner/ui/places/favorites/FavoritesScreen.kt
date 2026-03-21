package com.example.localguide_planner.ui.places.favorites

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.localguide_planner.ui.places.common.AnimatedPlaceCard
import com.example.localguide_planner.ui.places.common.toIcon
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesContent(
        uiState = uiState,
        onNavigateToDetail = onNavigateToDetail,
        onCategorySelected = viewModel::onCategorySelected,
        modifier = modifier,
    )
}

@Composable
internal fun FavoritesContent(
    uiState: FavoritesUiState,
    onNavigateToDetail: (String) -> Unit,
    onCategorySelected: (PlaceCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val deleteHintText = stringResource(R.string.favorites_delete_hint)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        FavoritesBody(
            uiState = uiState,
            onNavigateToDetail = onNavigateToDetail,
            onCategorySelected = onCategorySelected,
            onDeleteClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(deleteHintText)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun FavoritesBody(
    uiState: FavoritesUiState,
    onNavigateToDetail: (String) -> Unit,
    onCategorySelected: (PlaceCategory?) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        uiState.errorMessage != null -> {
            Box(modifier = modifier) {
                FavoritesErrorContent(modifier = Modifier.align(Alignment.Center))
            }
        }

        else -> {
            FavoritesListWithFilters(
                uiState = uiState,
                onNavigateToDetail = onNavigateToDetail,
                onCategorySelected = onCategorySelected,
                onDeleteClick = onDeleteClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FavoritesListWithFilters(
    uiState: FavoritesUiState,
    onNavigateToDetail: (String) -> Unit,
    onCategorySelected: (PlaceCategory?) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FavoritesCategoryFilter(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        FavoritesListContent(
            uiState = uiState,
            onNavigateToDetail = onNavigateToDetail,
            onDeleteClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesCategoryFilter(
    selectedCategory: PlaceCategory?,
    onCategorySelected: (PlaceCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.places_filter_all)) },
            )
        }
        items(PlaceCategory.entries) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.toStringRes())) },
                leadingIcon = {
                    Icon(
                        imageVector = category.toIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun FavoritesListContent(
    uiState: FavoritesUiState,
    onNavigateToDetail: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (uiState.places.isEmpty()) {
            EmptyFavoritesContent(modifier = Modifier.align(Alignment.Center))
        } else {
            FavoritesList(
                places = uiState.places,
                onPlaceClick = { place -> onNavigateToDetail(place.id) },
                onDeleteClick = { onDeleteClick() },
            )
        }
    }
}

@Composable
private fun FavoritesList(
    places: List<Place>,
    onPlaceClick: (Place) -> Unit,
    onDeleteClick: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items = places, key = { _, place -> place.id }) { index, place ->
            AnimatedPlaceCard(
                place = place,
                index = index,
                onClick = { onPlaceClick(place) },
                onDeleteClick = { onDeleteClick(place) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(
                        fadeInSpec = tween(
                            durationMillis = LocalGuideDesignTokens.animationDurationMedium,
                        ),
                        placementSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
            )
        }
    }
}

@Composable
private fun EmptyFavoritesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.favorites_empty_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.favorites_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FavoritesErrorContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.places_error_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true, name = "FavoritesScreen — with data")
@Composable
fun FavoritesScreenWithDataPreview() {
    LocalGuidePlannerTheme {
        FavoritesContent(
            uiState = FavoritesUiState(
                places = listOf(
                    Place(
                        id = "1",
                        name = "Центральный парк",
                        description = "Красивый городской парк",
                        category = PlaceCategory.PARK,
                        address = "ул. Ленина, 1",
                        latitude = 55.75,
                        longitude = 37.62,
                        isFavorite = true,
                        createdAt = System.currentTimeMillis(),
                    ),
                    Place(
                        id = "2",
                        name = "Городской музей",
                        description = "Исторический музей города",
                        category = PlaceCategory.MUSEUM,
                        address = "пр. Победы, 5",
                        latitude = null,
                        longitude = null,
                        isFavorite = true,
                        createdAt = System.currentTimeMillis(),
                    ),
                ),
                isLoading = false,
            ),
            onNavigateToDetail = {},
            onCategorySelected = {},
        )
    }
}

@Preview(showBackground = true, name = "FavoritesScreen — empty")
@Composable
fun FavoritesScreenEmptyPreview() {
    LocalGuidePlannerTheme {
        FavoritesContent(
            uiState = FavoritesUiState(
                places = emptyList(),
                isLoading = false,
            ),
            onNavigateToDetail = {},
            onCategorySelected = {},
        )
    }
}

@Preview(showBackground = true, name = "FavoritesScreen — loading")
@Composable
fun FavoritesScreenLoadingPreview() {
    LocalGuidePlannerTheme {
        FavoritesContent(
            uiState = FavoritesUiState(isLoading = true),
            onNavigateToDetail = {},
            onCategorySelected = {},
        )
    }
}
