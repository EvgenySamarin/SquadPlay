@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay.navigation

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.NewEventScreenAction
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.RegistrationScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.screens.auth.AuthScreen
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.event.NewEventScreen
import com.eysamarin.squadplay.screens.event.NewEventScreenViewModel
import com.eysamarin.squadplay.screens.main.HomeScreen
import com.eysamarin.squadplay.screens.main.HomeScreenViewModel
import com.eysamarin.squadplay.screens.profile.ProfileScreen
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
import com.eysamarin.squadplay.screens.registration.RegistrationScreen
import com.eysamarin.squadplay.screens.registration.RegistrationScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.reflect.typeOf

/**
 * Here you can define different flags based on windowsSize or just push it further to concrete screen
 *
 * WindowsSize class is no single way to support adaptivity, you also can use the DisplayFeatures
 * from accompanist library
 */
@Composable
fun SquadPlayNavigation(windowSize: WindowSizeClass) {

    val navController = rememberNavController()
    val navigator = koinInject<Navigator>()

    NavigationEffect(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> navController.navigate(action.destination) {
                action.navOptions(this)
            }

            NavigationAction.NavigateUp -> navController.navigateUp()
        }
    }

    NavHost(
        navController = navController,
        startDestination = navigator.startDestination
    ) {
        navigation<Destination.AuthGraph>(startDestination = Destination.AuthScreen) {
            composable<Destination.AuthScreen> {
                val viewModel: AuthScreenViewModel = koinViewModel()
                val snackbarHostState = remember { SnackbarHostState() }

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                AuthScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = {
                        when (it) {
                            AuthScreenAction.OnSignInWithGoogleTap -> viewModel.onSignInWithGoogleTap()
                            is AuthScreenAction.OnSignInTap -> viewModel.onSignInTap(
                                it.email,
                                it.password
                            )

                            AuthScreenAction.OnSignUpTap -> viewModel.onSignUpTap()
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    viewModel.snackbarFlow.collect {
                        snackbarHostState.showSnackbar(message = it)
                    }
                }
            }
            composable<Destination.RegistrationScreen> {
                val viewModel: RegistrationScreenViewModel = koinViewModel()
                val snackbarHostState = remember { SnackbarHostState() }

                RegistrationScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = { action ->
                        when (action) {
                            is RegistrationScreenAction.OnConfirmTap -> viewModel.onConfirmTap(
                                action.email,
                                action.password
                            )

                            RegistrationScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    viewModel.snackbarFlow.collect {
                        snackbarHostState.showSnackbar(message = it)
                    }
                }
            }
        }

        navigation<Destination.HomeGraph>(startDestination = Destination.HomeScreen()) {
            composable<Destination.HomeScreen>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://evgenysamarin.github.io/invite/{inviteGroupID}"
                    }
                ),
            ) { entry ->
                val viewModel: HomeScreenViewModel = koinViewModel()

                val groupId = remember { entry.arguments?.getString("inviteGroupID") }
                LaunchedEffect(groupId) {
                    viewModel.onJoinGroupDeepLinkRetrieved(groupId)
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val confirmInviteDialogState by viewModel.confirmInviteDialogState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                HomeScreen(
                    state = uiState,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    confirmInviteDialogState = confirmInviteDialogState,
                    windowSize = windowSize,
                    onAction = { action ->
                        when (action) {
                            is HomeScreenAction.OnDateTap -> viewModel.onDateTap(action.date)
                            is HomeScreenAction.OnNextMonthTap -> viewModel.onNextMonthTap(action.yearMonth)
                            is HomeScreenAction.OnPrevMonthTap -> viewModel.onPreviousMonthTap(action.yearMonth)
                            HomeScreenAction.OnAddGameEventTap -> viewModel.onAddGameEventTap()
                            HomeScreenAction.OnLogOutTap -> viewModel.onLogOutTap()
                            HomeScreenAction.OnAvatarTap -> viewModel.onAvatarTap()
                            HomeScreenAction.OnJoinGroupDialogConfirm -> {
                                viewModel.onJoinGroupDialogConfirm()
                                viewModel.onJoinGroupDialogDismiss()
                            }

                            HomeScreenAction.OnJoinGroupDialogDismiss -> viewModel.onJoinGroupDialogDismiss()
                            is HomeScreenAction.OnDeleteEventTap -> viewModel.onDeleteEventTap(action.eventId)
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    viewModel.snackbarFlow.collect {
                        snackbarHostState.showSnackbar(message = it)
                    }
                }
            }
            composable<Destination.NewEventScreen>(
                typeMap = mapOf(
                    typeOf<Date>() to Destination.NewEventScreen.CustomNavType.DateType,
                )
            ) { backStackEntry ->
                val viewModel: NewEventScreenViewModel = koinViewModel()
                val args = backStackEntry.toRoute<Destination.NewEventScreen>()
                viewModel.updateSelectedDate(args)
                Log.d("TAG", "yearMonthJsonFlow: ${args.yearMonth}")

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                NewEventScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = { action ->
                        when (action) {
                            NewEventScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
                            is NewEventScreenAction.OnStartPollingTap -> viewModel.onEventSaveTap(
                                dateTimeFrom = action.timeFrom,
                                dateTimeTo = action.timeTo,
                            )
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    viewModel.snackbarFlow.collect {
                        snackbarHostState.showSnackbar(message = it)
                    }
                }
            }
            composable<Destination.ProfileScreen> {
                val viewModel: ProfileScreenViewModel = koinViewModel()

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val inviteLinkState by viewModel.inviteLinkState.collectAsStateWithLifecycle()

                ProfileScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = { action ->
                        when (action) {
                            ProfileScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
                            ProfileScreenAction.OnCreateInviteLinkTap -> viewModel.onCreateInviteGroupLinkTap()
                            ProfileScreenAction.OnLogOutTap -> viewModel.onLogOutTap()
                        }
                    }
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
}

@Composable
private fun RootScreenBackHandler(
    snackbarHostState: SnackbarHostState,
) {
    val message = stringResource(R.string.press_again_to_exit)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000L) {
            backPressedTime = currentTime
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        } else {
            (context as? Activity)?.finish()
        }
    }
}

@Composable
fun <T> NavigationEffect(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner.lifecycle, key1, key2) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}
