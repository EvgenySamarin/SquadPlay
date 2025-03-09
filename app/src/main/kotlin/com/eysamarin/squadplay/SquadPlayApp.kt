@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.screens.main_screen.MainScreenViewModel
import com.eysamarin.squadplay.screens.main_screen.MainScreen
import org.koin.androidx.compose.koinViewModel

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
            val mainScreenViewModel: MainScreenViewModel = koinViewModel()
            val uiState by mainScreenViewModel.uiState.collectAsStateWithLifecycle()
            MainScreen(
                state = uiState,
                windowSize = windowSize,
                onAction = {
                    when (it) {
                        is MainScreenAction.OnDateTap -> mainScreenViewModel.onDateTap(it.date)
                        is MainScreenAction.OnNextMonthTap -> mainScreenViewModel
                            .onNextMonthTap(it.yearMonth)

                        is MainScreenAction.OnPrevMonthTap -> mainScreenViewModel
                            .onPreviousMonthTap(it.yearMonth)
                    }
                }
            )
        }
    }
}


sealed class Routes(val route: String) {
    data object Main : Routes(route = "Main")
}
