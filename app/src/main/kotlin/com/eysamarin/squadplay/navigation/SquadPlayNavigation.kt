@file:Suppress("kotlin:S1128")

package com.eysamarin.squadplay.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.EventDetailsScreenUI
import com.eysamarin.squadplay.models.SettingsScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.screens.auth.AuthScreen
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.event.EventDetailsScreen
import com.eysamarin.squadplay.screens.event.EventDetailsScreenViewModel
import com.eysamarin.squadplay.screens.event.NewEventScreen
import com.eysamarin.squadplay.screens.event.NewEventScreenViewModel
import com.eysamarin.squadplay.screens.main.HomeScreen
import com.eysamarin.squadplay.screens.main.HomeScreenViewModel
import com.eysamarin.squadplay.screens.profile.ProfileScreen
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
import com.eysamarin.squadplay.screens.registration.RegistrationScreen
import com.eysamarin.squadplay.screens.registration.RegistrationScreenViewModel
import com.eysamarin.squadplay.screens.settings.SettingsScreen
import com.eysamarin.squadplay.screens.settings.SettingsScreenViewModel
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.reflect.typeOf

@Composable
fun SquadPlayNavigation(
    windowSize: WindowSizeClass,
    startDestination: Destination,
) {

    val navController = rememberNavController()
    val navigator = koinInject<Navigator>()
    val analyticsProvider = koinInject<AnalyticsProvider>()

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.toAnalyticsScreenName()?.let { screenName ->
                analyticsProvider.trackScreenView(screenName)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarProvider = koinInject<SnackbarProvider>()
    val coroutineScope = rememberCoroutineScope()


    LifecycleEffect(snackbarProvider.messagesChannel) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message = it) }
    }

    LifecycleEffect(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> navController.navigate(action.destination) {
                action.navOptions(this)
            }

            NavigationAction.NavigateUp -> navController.navigateUp()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation<Destination.AuthGraph>(startDestination = Destination.AuthScreen) {
            composable<Destination.AuthScreen> {
                val viewModel: AuthScreenViewModel = koinViewModel()

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                AuthScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            composable<Destination.RegistrationScreen> {
                val viewModel: RegistrationScreenViewModel = koinViewModel()

                RegistrationScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
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
                val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
                val confirmInviteDialogState by viewModel.confirmInviteDialogState.collectAsStateWithLifecycle()

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                HomeScreen(
                    state = uiState,
                    isLoggingOut = isLoggingOut,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    confirmInviteDialogState = confirmInviteDialogState,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            composable<Destination.NewEventScreen>(
                typeMap = mapOf(
                    typeOf<Date>() to Destination.NewEventScreen.CustomNavType.DateType,
                )
            ) { backStackEntry ->
                val viewModel: NewEventScreenViewModel = koinViewModel()
                val args = backStackEntry.toRoute<Destination.NewEventScreen>()
                viewModel.updateSelectedDate(args)

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                NewEventScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            composable<Destination.EventDetailsScreen> { backStackEntry ->
                val viewModel: EventDetailsScreenViewModel = koinViewModel()
                val args = backStackEntry.toRoute<Destination.EventDetailsScreen>()
                LaunchedEffect(args) {
                    viewModel.initData(args)
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                EventDetailsScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            composable<Destination.ProfileScreen> {
                val viewModel: ProfileScreenViewModel = koinViewModel()

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
                val inviteLinkState by viewModel.inviteLinkState.collectAsStateWithLifecycle()

                ProfileScreen(
                    state = uiState,
                    isLoggingOut = isLoggingOut,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )

                if (inviteLinkState is UiState.Normal<String>) {
                    analyticsProvider.trackEvent(AnalyticsEvent.InviteShared)
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
            composable<Destination.SettingsScreen> {
                val viewModel: SettingsScreenViewModel = koinViewModel()

                val context = LocalContext.current
                val licensesMenuActivityTitle = stringResource(R.string.settings_screen_licenses_title)

                SettingsScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = { action ->
                        when (action) {
                            SettingsScreenAction.OnBackButtonTap -> viewModel.onBackButtonTap()
                            SettingsScreenAction.OnLicensesTap -> {
                                analyticsProvider.trackEvent(AnalyticsEvent.OssLicensesClicked)
                                OssLicensesMenuActivity.setActivityTitle(licensesMenuActivityTitle)
                                context.startActivity(
                                    Intent(context, OssLicensesMenuActivity::class.java)
                                )
                            }
                        }
                    },
                )
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
fun <T> LifecycleEffect(
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

fun NavDestination.toAnalyticsScreenName(): String? = when {
    hasRoute(Destination.HomeScreen::class) -> Destination.HomeScreen.SCREEN_NAME
    hasRoute(Destination.NewEventScreen::class) -> Destination.NewEventScreen.SCREEN_NAME
    hasRoute(Destination.EventDetailsScreen::class) -> Destination.EventDetailsScreen.SCREEN_NAME
    hasRoute(Destination.ProfileScreen::class) -> Destination.ProfileScreen.SCREEN_NAME
    hasRoute(Destination.SettingsScreen::class) -> Destination.SettingsScreen.SCREEN_NAME
    hasRoute(Destination.AuthScreen::class) -> Destination.AuthScreen.SCREEN_NAME
    hasRoute(Destination.RegistrationScreen::class) -> Destination.RegistrationScreen.SCREEN_NAME
    else -> null
}
