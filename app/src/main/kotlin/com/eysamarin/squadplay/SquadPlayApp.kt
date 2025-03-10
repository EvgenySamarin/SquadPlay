@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.screens.main_screen.MainScreen
import com.eysamarin.squadplay.screens.main_screen.MainScreenViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Here you can define different flags based on windowsSize or just push it further to concrete screen
 *
 * WindowsSize class is no single way to support adaptivity, you also can use the DisplayFeatures
 * from accompanist library
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadPlayApp(windowSize: WindowSizeClass) {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Main.route) {
        composable(Routes.Main.route) {
            val mainScreenViewModel: MainScreenViewModel = koinViewModel()
            val uiState by mainScreenViewModel.uiState.collectAsStateWithLifecycle()
            val pollingDialogState by mainScreenViewModel.pollingDialogState.collectAsStateWithLifecycle()

            MainScreen(
                state = uiState,
                pollingDialogState = pollingDialogState,
                windowSize = windowSize,
                onAction = {
                    handleMainScreenAction(action = it, viewModel = mainScreenViewModel)
                }
            )
        }
    }
}

private fun handleMainScreenAction(
    action: MainScreenAction,
    viewModel: MainScreenViewModel,
) = when (action) {
    is MainScreenAction.OnDateTap -> viewModel.onDateTap(action.date)
    is MainScreenAction.OnNextMonthTap -> viewModel.onNextMonthTap(action.yearMonth)
    is MainScreenAction.OnPrevMonthTap -> viewModel.onPreviousMonthTap(action.yearMonth)
    MainScreenAction.OnDismissPolingDialog -> viewModel.dismissPolingDialog()
    is MainScreenAction.OnPollingStartTap -> viewModel.onPollingStartTap(
        timeFrom = action.timeFrom,
        timeTo = action.timeTo,
    )

    MainScreenAction.OnAddGameEventTap -> viewModel.onAddGameEventTap()
}


sealed class Routes(val route: String) {
    data object Main : Routes(route = "Main")
}
