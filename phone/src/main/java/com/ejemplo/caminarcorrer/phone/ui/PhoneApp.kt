package com.ejemplo.caminarcorrer.phone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejemplo.caminarcorrer.phone.PhoneAppContainer
import com.ejemplo.caminarcorrer.phone.ui.screens.DetailScreen
import com.ejemplo.caminarcorrer.phone.ui.screens.HistoryScreen
import com.ejemplo.caminarcorrer.phone.ui.screens.HomeScreen
import com.ejemplo.caminarcorrer.phone.ui.screens.ProfileScreen

@Composable
fun PhoneApp(container: PhoneAppContainer) {
    val viewModel: PhoneViewModel = viewModel(factory = PhoneViewModel.factory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                uiState = uiState,
                dataLayerAvailable = viewModel.dataLayerAvailable,
                onHistory = { navController.navigate("history") },
                onProfile = { navController.navigate("profile") },
                onSync = viewModel::requestSync,
                onClearMessage = viewModel::clearMessage
            )
        }
        composable("history") {
            HistoryScreen(
                activities = uiState.activities,
                onBack = { navController.popBackStack() },
                onActivityClick = { activityId ->
                    navController.navigate("detail/$activityId")
                }
            )
        }
        composable(
            route = "detail/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId")
            DetailScreen(
                activitiesLoaded = uiState.activities != null,
                activity = uiState.activities?.firstOrNull { it.id == activityId },
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                profile = uiState.profile,
                message = uiState.syncMessage,
                onBack = { navController.popBackStack() },
                onSave = viewModel::saveWeight,
                onClearMessage = viewModel::clearMessage
            )
        }
    }
}
