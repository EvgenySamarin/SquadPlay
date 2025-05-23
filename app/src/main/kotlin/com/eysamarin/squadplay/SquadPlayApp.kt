@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.RegistrationScreenAction
import com.eysamarin.squadplay.models.Route
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.screens.auth.AuthScreen
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.main.MainScreen
import com.eysamarin.squadplay.screens.main.MainScreenViewModel
import com.eysamarin.squadplay.screens.profile.ProfileScreen
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
import com.eysamarin.squadplay.screens.registration.RegistrationScreen
import com.eysamarin.squadplay.screens.registration.RegistrationScreenViewModel
import com.eysamarin.squadplay.utils.findActivity
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
    NavHost(navController = navController, startDestination = Route.Auth.route) {
        composable(Route.Auth.route) {
            val viewModel: AuthScreenViewModel = koinViewModel()
            val snackbarHostState = remember { SnackbarHostState() }

            AuthScreen(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
                windowSize = windowSize,
                onAction = {
                    handleAuthScreenAction(action = it, viewModel = viewModel)
                }
            )

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Route.Auth,
            )

            LaunchedEffect(Unit) {
                viewModel.snackbarFlow.collect {
                    snackbarHostState.showSnackbar(message = it)
                }
            }
        }
        composable(Route.Registration.route) {
            val viewModel: RegistrationScreenViewModel = koinViewModel()
            val snackbarHostState = remember { SnackbarHostState() }

            RegistrationScreen(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
                windowSize = windowSize,
                onAction = {
                    handleRegistrationScreenAction(action = it, viewModel = viewModel)
                }
            )

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Route.Auth,
            )

            LaunchedEffect(Unit) {
                viewModel.snackbarFlow.collect {
                    snackbarHostState.showSnackbar(message = it)
                }
            }
        }
        composable(
            route = Route.Main.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://evgenysamarin.github.io/invite/{inviteGroupID}"
                }
            ),
            arguments = listOf(
                navArgument("inviteGroupID") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                }
            )
        ) { entry ->
            val viewModel: MainScreenViewModel = koinViewModel()

            val groupId = remember { entry.arguments?.getString("inviteGroupID") }
            LaunchedEffect(groupId) {
                viewModel.onJoinGroupDeepLinkRetrieved(groupId)
            }

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val eventDialogState by viewModel.eventDialogState.collectAsStateWithLifecycle()
            val confirmInviteDialogState by viewModel.confirmInviteDialogState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            MainScreen(
                state = uiState,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
                eventDialogState = eventDialogState,
                confirmInviteDialogState = confirmInviteDialogState,
                windowSize = windowSize,
                onAction = {
                    handleMainScreenAction(action = it, viewModel = viewModel)
                }
            )

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Route.Main,
            )

            LaunchedEffect(Unit) {
                viewModel.snackbarFlow.collect {
                    snackbarHostState.showSnackbar(message = it)
                }
            }
        }
        composable(Route.Profile.route) {
            val viewModel: ProfileScreenViewModel = koinViewModel()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val inviteLinkState by viewModel.inviteLinkState.collectAsStateWithLifecycle()

            ProfileScreen(
                state = uiState,
                windowSize = windowSize,
                onAction = {
                    handleProfileScreenAction(action = it, viewModel = viewModel)
                }
            )

            NavigationEffect(
                navigationFlow = viewModel.navigationFlow,
                navController = navController,
                startDestination = Route.Main,
            )

            if (inviteLinkState is UiState.Normal<String>) {
                val inviteLink = (inviteLinkState as UiState.Normal<String>).data
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, inviteLink)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                LocalContext.current.startActivity(shareIntent)
                viewModel.hideShareLink()
            }
        }
    }
}

@Composable
private fun NavigationEffect(
    navigationFlow: Flow<NavAction>,
    navController: NavHostController,
    startDestination: Route,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(
        enabled = currentRoute == Route.Main.route,
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            context.findActivity()?.finish()
            return@BackHandler
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

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
    AuthScreenAction.OnSignInWithGoogleTap -> viewModel.onSignInWithGoogleTap()
    is AuthScreenAction.OnSignInTap -> viewModel.onSignInTap(action.email, action.password)
    AuthScreenAction.OnSignUpTap -> viewModel.onSignUpTap()
}

private fun handleRegistrationScreenAction(
    action: RegistrationScreenAction,
    viewModel: RegistrationScreenViewModel,
) = when (action) {
    is RegistrationScreenAction.OnConfirmTap -> viewModel.onConfirmTap(action.email, action.password)
    RegistrationScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
}

private fun handleProfileScreenAction(
    action: ProfileScreenAction,
    viewModel: ProfileScreenViewModel,
) = when (action) {
    ProfileScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
    ProfileScreenAction.OnCreateInviteLinkTap -> viewModel.onCreateInviteGroupLinkTap()
    ProfileScreenAction.OnLogOutTap -> viewModel.onLogOutTap()
}

private fun handleMainScreenAction(
    action: MainScreenAction,
    viewModel: MainScreenViewModel,
) = when (action) {
    is MainScreenAction.OnDateTap -> viewModel.onDateTap(action.date)
    is MainScreenAction.OnNextMonthTap -> viewModel.onNextMonthTap(action.yearMonth)
    is MainScreenAction.OnPrevMonthTap -> viewModel.onPreviousMonthTap(action.yearMonth)
    MainScreenAction.OnDismissEventDialog -> viewModel.dismissEventDialog()
    is MainScreenAction.OnEventSaveTap -> viewModel.onEventSaveTap(
        dateTimeFrom = action.dateTimeFrom,
        dateTimeTo = action.dateTimeTo,
    )

    MainScreenAction.OnAddGameEventTap -> viewModel.onAddGameEventTap()
    MainScreenAction.OnLogOutTap -> viewModel.onLogOutTap()
    MainScreenAction.OnAvatarTap -> viewModel.onAvatarTap()
    MainScreenAction.OnJoinGroupDialogConfirm -> {
        viewModel.onJoinGroupDialogConfirm()
        viewModel.onJoinGroupDialogDismiss()
    }

    MainScreenAction.OnJoinGroupDialogDismiss -> viewModel.onJoinGroupDialogDismiss()
    is MainScreenAction.OnDeleteEventTap ->  viewModel.onDeleteEventTap(action.eventId)
}
