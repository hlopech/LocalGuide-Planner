package com.example.localguide_planner.ui.map

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val DEFAULT_LAT = 55.7558
private const val DEFAULT_LON = 37.6173
private const val DEFAULT_ZOOM = 10.0
private const val MARKER_SIZE_DP = 32
private const val OSMDROID_PREFS = "osmdroid"

@Composable
fun MapScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MapContent(
        uiState = uiState,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}

@Composable
internal fun MapContent(
    uiState: MapUiState,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (LocalInspectionMode.current) {
            MapPreviewPlaceholder()
        } else {
            OsmMapView(
                places = uiState.places,
                onMarkerClick = onNavigateToDetail,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun MapPreviewPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize())
}

@Composable
private fun OsmMapView(
    places: List<Place>,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember {
        mutableStateOf(createMapView(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.value.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.value.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView.value },
        update = { mv ->
            updateMapMarkers(mv, places, context, onMarkerClick)
            val firstWithCoords = places.firstOrNull { it.latitude != null && it.longitude != null }
            if (firstWithCoords != null) {
                mv.controller.setCenter(
                    GeoPoint(firstWithCoords.latitude!!, firstWithCoords.longitude!!),
                )
            }
            mv.invalidate()
        },
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE),
    )
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(DEFAULT_ZOOM)
        controller.setCenter(GeoPoint(DEFAULT_LAT, DEFAULT_LON))
    }
}

private fun updateMapMarkers(
    mapView: MapView,
    places: List<Place>,
    context: Context,
    onMarkerClick: (String) -> Unit,
) {
    mapView.overlays.clear()
    places
        .filter { it.latitude != null && it.longitude != null }
        .forEach { place ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(place.latitude!!, place.longitude!!)
            marker.title = place.name
            marker.icon = createMarkerDrawable(context, place.category.toMapMarkerColor())
            marker.setOnMarkerClickListener { _, _ ->
                onMarkerClick(place.id)
                true
            }
            mapView.overlays.add(marker)
        }
    mapView.invalidate()
}

private fun createMarkerDrawable(context: Context, color: Color): GradientDrawable {
    val sizePx = (MARKER_SIZE_DP * context.resources.displayMetrics.density).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color.toArgb())
        setSize(sizePx, sizePx)
    }
}

@Preview(showBackground = true, name = "MapScreen — loading")
@Composable
fun MapScreenPreview() {
    LocalGuidePlannerTheme {
        MapContent(
            uiState = MapUiState(isLoading = true),
            onNavigateToDetail = {},
        )
    }
}

@Preview(showBackground = true, name = "MapScreen — loading internal")
@Composable
private fun MapContentLoadingPreview() {
    LocalGuidePlannerTheme {
        MapContent(
            uiState = MapUiState(isLoading = true),
            onNavigateToDetail = {},
        )
    }
}

@Preview(showBackground = true, name = "MapScreen — с местами")
@Composable
private fun MapContentWithPlacesPreview() {
    LocalGuidePlannerTheme {
        MapContent(
            uiState = MapUiState(
                places = listOf(
                    Place(
                        id = "1",
                        name = "Парк",
                        description = "",
                        category = PlaceCategory.PARK,
                        address = "ул. Ленина, 1",
                        latitude = 55.75,
                        longitude = 37.62,
                        isFavorite = false,
                        createdAt = 0L,
                    ),
                ),
                isLoading = false,
            ),
            onNavigateToDetail = {},
        )
    }
}
