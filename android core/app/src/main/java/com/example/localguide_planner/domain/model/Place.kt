package com.example.localguide_planner.domain.model

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean,
    val createdAt: Long
)

enum class PlaceCategory {
    RESTAURANT, CAFE, PARK, MUSEUM, SHOP, LANDMARK, OTHER
}
