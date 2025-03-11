@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.Routes
import com.eysamarin.squadplay.screens.auth.AuthScreen
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.main.MainScreen
import com.eysamarin.squadplay.screens.main.MainScreenViewModel
import kotlinx.coroutines.flow.Flow
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
    NavHost(navController = navController, startDestination = Routes.Auth.route) {
        composable(Routes.Auth.route) {
            val viewModel: AuthScreenViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Routes.Auth,
            )

            AuthScreen(
                state = uiState,
                windowSize = windowSize,
                onAction = {
                    handleAuthScreenAction(action = it, viewModel = viewModel)
                }
            )
        }
        composable(Routes.Main.route) {
            val viewModel: MainScreenViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val pollingDialogState by viewModel.pollingDialogState.collectAsStateWithLifecycle()

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Routes.Main,
            )

            MainScreen(
                state = uiState,
                pollingDialogState = pollingDialogState,
                windowSize = windowSize,
                onAction = {
                    handleMainScreenAction(action = it, viewModel = viewModel)
                }
            )
        }
    }
}

@Composable
private fun NavigationEffect(
    navigationFlow: Flow<NavAction>,
    navController: NavHostController,
    startDestination: Routes,
) {
    LaunchedEffect(Unit) {
        navigationFlow.collect { action ->
            when (action) {
                is NavAction.NavigateTo -> navController.navigate(action.route)
                NavAction.NavigateBack -> navController.popBackStack()
                NavAction.PopToStart -> navController.popBackStack(
                    route = startDestination.route,
                    inclusive = false
                )
            }
        }
    }
}

private fun handleAuthScreenAction(
    action: AuthScreenAction,
    viewModel: AuthScreenViewModel,
) = when (action) {
    AuthScreenAction.OnSignUpTap -> viewModel.onSignUpTap()
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
    MainScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
}
