package com.example.localguide_planner.ui.theme

import androidx.compose.ui.unit.dp

object LocalGuideDesignTokens {

    // Gradients (for hero sections and cards)
    val heroGradientColors = listOf(GradientHeroStart, GradientHeroEnd)
    val cardGradientLight = listOf(GradientOverlayTransparent, GradientOverlaySemiDark)

    // Elevation / shadows
    val cardElevation = 2.dp
    val fabElevation = 6.dp

    // Category accent colours
    val categoryColorRestaurant = CategoryRestaurant
    val categoryColorCafe = CategoryCafe
    val categoryColorPark = CategoryPark
    val categoryColorMuseum = CategoryMuseum
    val categoryColorShop = CategoryShop
    val categoryColorLandmark = CategoryLandmark
    val categoryColorOther = CategoryOther

    // Animation durations (milliseconds)
    val animationDurationShort = 200
    val animationDurationMedium = 350
    val animationDurationLong = 500

    // Spacing
    val screenPaddingHorizontal = 16.dp
    val cardCornerRadius = 24.dp
    val heroSectionHeight = 200.dp
}
