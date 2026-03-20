package com.example.localguide_planner.ui.places.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Museum
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens

@StringRes
internal fun PlaceCategory.toStringRes(): Int = when (this) {
    PlaceCategory.RESTAURANT -> R.string.category_restaurant
    PlaceCategory.CAFE -> R.string.category_cafe
    PlaceCategory.PARK -> R.string.category_park
    PlaceCategory.MUSEUM -> R.string.category_museum
    PlaceCategory.SHOP -> R.string.category_shop
    PlaceCategory.LANDMARK -> R.string.category_landmark
    PlaceCategory.OTHER -> R.string.category_other
}

internal fun PlaceCategory.toAccentColor(): Color = when (this) {
    PlaceCategory.RESTAURANT -> LocalGuideDesignTokens.categoryColorRestaurant
    PlaceCategory.CAFE -> LocalGuideDesignTokens.categoryColorCafe
    PlaceCategory.PARK -> LocalGuideDesignTokens.categoryColorPark
    PlaceCategory.MUSEUM -> LocalGuideDesignTokens.categoryColorMuseum
    PlaceCategory.SHOP -> LocalGuideDesignTokens.categoryColorShop
    PlaceCategory.LANDMARK -> LocalGuideDesignTokens.categoryColorLandmark
    PlaceCategory.OTHER -> LocalGuideDesignTokens.categoryColorOther
}

internal fun PlaceCategory.toIcon(): ImageVector = when (this) {
    PlaceCategory.RESTAURANT -> Icons.Rounded.Restaurant
    PlaceCategory.CAFE -> Icons.Rounded.LocalCafe
    PlaceCategory.PARK -> Icons.Rounded.Park
    PlaceCategory.MUSEUM -> Icons.Rounded.Museum
    PlaceCategory.SHOP -> Icons.Rounded.ShoppingBag
    PlaceCategory.LANDMARK -> Icons.Rounded.AccountBalance
    PlaceCategory.OTHER -> Icons.Rounded.Place
}
