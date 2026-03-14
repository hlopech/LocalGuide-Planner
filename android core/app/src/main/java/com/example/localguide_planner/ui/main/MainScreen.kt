package com.example.localguide_planner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.localguide_planner.R
import com.example.localguide_planner.ui.navigation.Screen
import com.example.localguide_planner.ui.theme.LocalGuide_PlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_bar_title_places)) },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Filled.Place, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.nav_places)) },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == Screen.PlacesList.route
                    } == true,
                    onClick = {
                        nestedNavController.navigate(Screen.PlacesList.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.PlacesList.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = Screen.PlacesList.route) {
                PlacesListPlaceholder()
            }
        }
    }
}

@Composable
private fun PlacesListPlaceholder() {
    Text(text = "Places list placeholder")
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LocalGuide_PlannerTheme {
        MainScreen()
    }
}
