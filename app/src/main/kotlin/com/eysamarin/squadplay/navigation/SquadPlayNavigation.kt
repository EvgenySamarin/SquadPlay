package com.eysamarin.squadplay.navigation

import android.app.Activity
import android.content.Intent
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.SettingsScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.screens.auth.AuthScreen
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.event.EventDetailsScreen
import com.eysamarin.squadplay.screens.event.EventDetailsScreenViewModel
import com.eysamarin.squadplay.screens.event.NewEventScreen
import com.eysamarin.squadplay.screens.event.NewEventScreenViewModel
import com.eysamarin.squadplay.screens.main.ConfirmationDialog
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

@Composable
fun SquadPlayNavigation(
    windowSize: WindowSizeClass,
    startDestination: Destination,
) {
    val initialKey: NavKey = when (startDestination) {
        Destination.HomeGraph -> Destination.HomeScreen
        Destination.AuthGraph -> Destination.AuthScreen
        else -> startDestination
    }

    val backStack = rememberNavBackStack(initialKey)
    val navigator = koinInject<Navigator>()
    val analyticsProvider = koinInject<AnalyticsProvider>()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarProvider = koinInject<SnackbarProvider>()
    val coroutineScope = rememberCoroutineScope()

    LifecycleEffect(snackbarProvider.messagesChannel) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message = it) }
    }

    LifecycleEffect(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> {
                when (action.destination) {
                    Destination.HomeGraph -> {
                        backStack.clear()
                        backStack.add(Destination.HomeScreen)
                    }
                    Destination.AuthGraph -> {
                        backStack.clear()
                        backStack.add(Destination.AuthScreen)
                    }
                    else -> {
                        backStack.add(action.destination)
                    }
                }
            }
            NavigationAction.NavigateUp -> {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            }
        }
    }

    LaunchedEffect(backStack.lastOrNull()) {
        (backStack.lastOrNull() as? Destination)?.screenName?.let { screenName ->
            analyticsProvider.trackScreenView(screenName)
        }
    }

    val currentKey = backStack.lastOrNull()
    val isInHomeGraph = currentKey is Destination.HomeScreen ||
            currentKey is Destination.NewEventScreen ||
            currentKey is Destination.EventDetailsScreen ||
            currentKey is Destination.ProfileScreen ||
            currentKey is Destination.SettingsScreen

    if (isInHomeGraph) {
        HomeGraphDeepLinkHandler(windowSize = windowSize)
    }

    val saveableDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val decorators = remember(saveableDecorator) { listOf(saveableDecorator) }

    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = decorators,
        entryProvider = entryProvider {
            entry<Destination.AuthScreen> {
                val viewModel: AuthScreenViewModel = koinViewModel()

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                AuthScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            entry<Destination.RegistrationScreen> {
                val viewModel: RegistrationScreenViewModel = koinViewModel()

                RegistrationScreen(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            entry<Destination.HomeScreen> {
                val viewModel: HomeScreenViewModel = koinViewModel()

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
                val isTimeoutDialogVisible by viewModel.isTimeoutDialogVisible.collectAsStateWithLifecycle()

                RootScreenBackHandler(snackbarHostState = snackbarHostState)

                HomeScreen(
                    state = uiState,
                    isLoggingOut = isLoggingOut,
                    isTimeoutDialogVisible = isTimeoutDialogVisible,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            entry<Destination.NewEventScreen> { key ->
                val viewModel: NewEventScreenViewModel = koinViewModel()
                LaunchedEffect(key) {
                    viewModel.updateSelectedDate(key)
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                NewEventScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            entry<Destination.EventDetailsScreen> { key ->
                val viewModel: EventDetailsScreenViewModel = koinViewModel()
                LaunchedEffect(key) {
                    viewModel.initData(key)
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                EventDetailsScreen(
                    state = uiState,
                    windowSize = windowSize,
                    onAction = viewModel::onAction,
                )
            }
            entry<Destination.ProfileScreen> {
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
            entry<Destination.SettingsScreen> {
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
    )

    NavDisplay(
        entries = entries,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }
    )
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

@Composable
private fun HomeGraphDeepLinkHandler(
    windowSize: WindowSizeClass,
) {
    val viewModel: HomeGraphViewModel = koinViewModel()
    val confirmInviteDialogState by viewModel.confirmInviteDialogState.collectAsStateWithLifecycle()

    if (confirmInviteDialogState is UiState.Normal<String>) {
        ConfirmationDialog(
            windowSize = windowSize,
            title = stringResource(R.string.invite_new_friend),
            text = (confirmInviteDialogState as UiState.Normal<String>).data,
            onDismiss = viewModel::onJoinGroupDialogDismiss,
            onConfirmTap = viewModel::onJoinGroupDialogConfirm,
        )
    }
}
