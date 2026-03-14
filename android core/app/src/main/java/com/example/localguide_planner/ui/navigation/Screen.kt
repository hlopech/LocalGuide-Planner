package com.example.localguide_planner.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
    data object PlacesList : Screen("places/list")

    data class PlaceDetail(val placeId: String = "{placeId}") :
        Screen("places/detail/$placeId") {
        companion object {
            const val ROUTE = "places/detail/{placeId}"
            const val ARG_PLACE_ID = "placeId"
        }
    }

    data object AddPlace : Screen("places/add")

    data class EditPlace(val placeId: String = "{placeId}") :
        Screen("places/edit/$placeId") {
        companion object {
            const val ROUTE = "places/edit/{placeId}"
            const val ARG_PLACE_ID = "placeId"
        }
    }
}
