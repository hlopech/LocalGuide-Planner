package com.example.localguide_planner.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localguide_planner.R
import com.example.localguide_planner.ui.main.MainScreen
import com.example.localguide_planner.ui.theme.LocalGuide_PlannerTheme

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Onboarding.route) {
            // TODO(TASK-004): заменить на OnboardingScreen(onCompleted = onOnboardingCompleted)
            val onOnboardingCompleted: () -> Unit = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
            // Placeholder — будет заменён на OnboardingScreen после мержа TASK-004
            Text(text = stringResource(R.string.placeholder_onboarding))
        }

        composable(route = Screen.Main.route) {
            MainScreen()
        }

        composable(
            route = Screen.PlaceDetail.ROUTE,
            arguments = listOf(
                navArgument(Screen.PlaceDetail.ARG_PLACE_ID) { type = NavType.StringType },
            ),
        ) {
            Text(text = stringResource(R.string.placeholder_place_detail))
        }

        composable(route = Screen.AddPlace.route) {
            Text(text = stringResource(R.string.placeholder_add_place))
        }

        composable(
            route = Screen.EditPlace.ROUTE,
            arguments = listOf(
                navArgument(Screen.EditPlace.ARG_PLACE_ID) { type = NavType.StringType },
            ),
        ) {
            Text(text = stringResource(R.string.placeholder_edit_place))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavGraphPreview() {
    LocalGuide_PlannerTheme {
        AppNavGraph(startDestination = Screen.Main.route)
    }
}
