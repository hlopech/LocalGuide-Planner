package com.example.localguide_planner.ui.map

import androidx.compose.ui.graphics.Color
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.theme.LocalGuideDesignTokens

internal fun PlaceCategory.toMapMarkerColor(): Color = when (this) {
    PlaceCategory.RESTAURANT -> LocalGuideDesignTokens.categoryColorRestaurant
    PlaceCategory.CAFE -> LocalGuideDesignTokens.categoryColorCafe
    PlaceCategory.PARK -> LocalGuideDesignTokens.categoryColorPark
    PlaceCategory.MUSEUM -> LocalGuideDesignTokens.categoryColorMuseum
    PlaceCategory.SHOP -> LocalGuideDesignTokens.categoryColorShop
    PlaceCategory.LANDMARK -> LocalGuideDesignTokens.categoryColorLandmark
    PlaceCategory.OTHER -> LocalGuideDesignTokens.categoryColorOther
}
