package com.example.localguide_planner.ui.places.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.Place
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            PlaceCardHeader(place = place, onDeleteClick = onDeleteClick)
            Spacer(modifier = Modifier.height(4.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(text = stringResource(place.category.toStringRes())) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaceCardHeader(
    place: Place,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        PlaceCardActions(isFavorite = place.isFavorite, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun PlaceCardActions(
    isFavorite: Boolean,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.place_card_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true)
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

@Preview(showBackground = true, name = "PlaceCard — not favorite")
@Composable
fun PlaceCardNotFavoritePreview() {
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
