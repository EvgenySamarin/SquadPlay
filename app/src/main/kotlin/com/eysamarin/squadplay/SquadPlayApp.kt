@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eysamarin.squadplay.screens.main_screen.MainViewModel
import com.eysamarin.squadplay.screens.main_screen.MainScreen

/**
 * Here you can define different flags based on windowsSize or just push it further to concrete screen
 *
 * WindowsSize class is no single way to support adaptivity, you also can use the DisplayFeatures
 * from accompanist library
 */
@Composable
fun SquadPlayApp(windowSize: WindowSizeClass) {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Main.route) {
        composable(Routes.Main.route) {
            val mainViewModel = MainViewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            MainScreen(
                state = uiState,
                windowSize = windowSize,
            )
        }
    }
}


sealed class Routes(val route: String) {
    data object Main : Routes(route = "Main")
}
