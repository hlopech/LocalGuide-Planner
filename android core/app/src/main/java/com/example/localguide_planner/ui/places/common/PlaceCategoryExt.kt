package com.example.localguide_planner.ui.places.common

import androidx.annotation.StringRes
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.PlaceCategory

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
