package com.example.localguide_planner.ui.places.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun PlacesListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlacesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlacesListContent(
        uiState = uiState,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAdd = onNavigateToAdd,
        onDeletePlace = { placeId -> viewModel.deletePlace(placeId) },
        modifier = modifier,
    )
}

@Composable
internal fun PlacesListContent(
    uiState: PlacesListUiState,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onDeletePlace: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorText = stringResource(R.string.places_error_loading)

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(errorText)
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.places_list_fab_add),
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        PlacesListBody(
            uiState = uiState,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAdd = onNavigateToAdd,
            onDeletePlace = onDeletePlace,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun PlacesListBody(
    uiState: PlacesListUiState,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onDeletePlace: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            uiState.places.isEmpty() -> {
                EmptyPlacesContent(
                    onAddClick = onNavigateToAdd,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                PlacesList(
                    places = uiState.places,
                    onPlaceClick = { place -> onNavigateToDetail(place.id) },
                    onDeleteClick = { place -> onDeletePlace(place.id) },
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.places_error_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun EmptyPlacesContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.places_list_empty_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Text(text = stringResource(R.string.places_list_empty_action))
        }
    }
}

@Composable
private fun PlacesList(
    places: List<Place>,
    onPlaceClick: (Place) -> Unit,
    onDeleteClick: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = places, key = { it.id }) { place ->
            PlaceCard(
                place = place,
                onClick = { onPlaceClick(place) },
                onDeleteClick = { onDeleteClick(place) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "PlacesListScreen — with places")
@Composable
fun PlacesListScreenWithDataPreview() {
    LocalGuidePlannerTheme {
        PlacesListContent(
            uiState = PlacesListUiState(
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
                        name = "Кофейня «Уют»",
                        description = "Уютное кафе в центре",
                        category = PlaceCategory.CAFE,
                        address = "пр. Победы, 15",
                        latitude = null,
                        longitude = null,
                        isFavorite = false,
                        createdAt = System.currentTimeMillis(),
                    ),
                ),
                isLoading = false,
            ),
            onNavigateToDetail = {},
            onNavigateToAdd = {},
            onDeletePlace = {},
        )
    }
}

@Preview(showBackground = true, name = "PlacesListScreen — empty")
@Composable
fun PlacesListScreenEmptyPreview() {
    LocalGuidePlannerTheme {
        PlacesListContent(
            uiState = PlacesListUiState(places = emptyList(), isLoading = false),
            onNavigateToDetail = {},
            onNavigateToAdd = {},
            onDeletePlace = {},
        )
    }
}

@Preview(showBackground = true, name = "PlacesListScreen — loading")
@Composable
fun PlacesListScreenLoadingPreview() {
    LocalGuidePlannerTheme {
        PlacesListContent(
            uiState = PlacesListUiState(isLoading = true),
            onNavigateToDetail = {},
            onNavigateToAdd = {},
            onDeletePlace = {},
        )
    }
}

@Preview(showBackground = true, name = "PlacesListScreen — error")
@Composable
fun PlacesListScreenErrorPreview() {
    LocalGuidePlannerTheme {
        PlacesListContent(
            uiState = PlacesListUiState(isLoading = false, errorMessage = "error"),
            onNavigateToDetail = {},
            onNavigateToAdd = {},
            onDeletePlace = {},
        )
    }
}
