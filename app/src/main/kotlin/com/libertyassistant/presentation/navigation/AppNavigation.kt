package com.libertyassistant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.libertyassistant.presentation.ui.screen.HomeScreen
import com.libertyassistant.presentation.ui.screen.JournalScreen
import com.libertyassistant.presentation.ui.screen.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Journal : Screen("journal")
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Journal.route) {
            JournalScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
