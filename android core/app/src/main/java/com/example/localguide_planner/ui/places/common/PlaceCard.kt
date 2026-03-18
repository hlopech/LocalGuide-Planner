package com.example.localguide_planner.ui.places.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.theme.FavoriteRed
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme
import kotlinx.coroutines.delay

@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = LocalGuideDesignTokens.cardElevation,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAccentBox(category = place.category)
            Spacer(modifier = Modifier.width(12.dp))
            PlaceCardContent(place = place, modifier = Modifier.weight(1f))
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.place_card_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun PlaceCardContent(
    place: Place,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = place.address,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(text = stringResource(place.category.toStringRes()))
                },
            )
            if (place.isFavorite) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = FavoriteRed,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryAccentBox(
    category: PlaceCategory,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = category.toAccentColor(),
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.toIcon(),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun AnimatedPlaceCard(
    place: Place,
    index: Int,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(place.id) {
        delay(minOf(index.toLong() * 60L, 300L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(LocalGuideDesignTokens.animationDurationMedium)) +
            slideInVertically(tween(LocalGuideDesignTokens.animationDurationMedium)) { it / 3 },
    ) {
        PlaceCard(
            place = place,
            onClick = onClick,
            onDeleteClick = onDeleteClick,
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true, name = "PlaceCard — park favorite")
@Composable
fun PlaceCardPreview() {
    LocalGuidePlannerTheme {
        PlaceCard(
            place = Place(
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
            onClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true, name = "PlaceCard — cafe not favorite")
@Composable
fun PlaceCardCafePreview() {
    LocalGuidePlannerTheme {
        PlaceCard(
            place = Place(
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
            onClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true, name = "PlaceCard — restaurant")
@Composable
fun PlaceCardRestaurantPreview() {
    LocalGuidePlannerTheme {
        PlaceCard(
            place = Place(
                id = "3",
                name = "Ресторан «Восток»",
                description = "Восточная кухня",
                category = PlaceCategory.RESTAURANT,
                address = "пр. Мира, 42",
                latitude = null,
                longitude = null,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
            ),
            onClick = {},
            onDeleteClick = {},
        )
    }
}
