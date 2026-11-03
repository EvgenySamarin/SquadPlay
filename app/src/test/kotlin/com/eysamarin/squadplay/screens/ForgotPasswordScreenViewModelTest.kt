package com.eysamarin.squadplay.screens

import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarMessage
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.ForgotPasswordScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.auth.AuthScreenViewModel
import com.eysamarin.squadplay.screens.forgotpassword.ForgotPasswordScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSendResetEmailTap success shows confirmation snackbar, tracks analytics, and navigates up`() = runTest {
        val navigator = FakeNavigator()
        val snackbar = FakeSnackbarProvider()
        val authProvider = FakeAuthProvider(resetResult = UiState.Normal(true))
        val stringProvider = FakeStringProvider()
        val analytics = FakeAnalyticsProvider()
        val viewModel = ForgotPasswordScreenViewModel(
            navigator = navigator,
            snackbar = snackbar,
            authProvider = authProvider,
            stringProvider = stringProvider,
            analyticsProvider = analytics,
            logger = FakeAppLogger(),
        )

        viewModel.onAction(ForgotPasswordScreenAction.OnSendResetEmailTap("player@squadplay.com"))
        advanceUntilIdle()

        assertEquals("player@squadplay.com", authProvider.lastResetEmail)
        assertTrue(analytics.trackedEvents.contains(AnalyticsEvent.PasswordResetRequested))
        assertEquals(listOf("Password reset email has been sent"), snackbar.shownMessages)
        assertEquals(1, navigator.navigateUpCalls)
    }

    @Test
    fun `onSendResetEmailTap error shows error snackbar and does not navigate up`() = runTest {
        val navigator = FakeNavigator()
        val snackbar = FakeSnackbarProvider()
        val authProvider = FakeAuthProvider(resetResult = UiState.Error("Invalid email address"))
        val stringProvider = FakeStringProvider()
        val analytics = FakeAnalyticsProvider()
        val viewModel = ForgotPasswordScreenViewModel(
            navigator = navigator,
            snackbar = snackbar,
            authProvider = authProvider,
            stringProvider = stringProvider,
            analyticsProvider = analytics,
            logger = FakeAppLogger(),
        )

        viewModel.onAction(ForgotPasswordScreenAction.OnSendResetEmailTap("bad-email"))
        advanceUntilIdle()

        assertEquals("bad-email", authProvider.lastResetEmail)
        assertTrue(analytics.trackedEvents.contains(AnalyticsEvent.PasswordResetRequested))
        assertEquals(listOf("Invalid email address"), snackbar.shownMessages)
        assertEquals(0, navigator.navigateUpCalls)
    }

    @Test
    fun `onBackButtonTap navigates up`() = runTest {
        val navigator = FakeNavigator()
        val viewModel = ForgotPasswordScreenViewModel(
            navigator = navigator,
            snackbar = FakeSnackbarProvider(),
            authProvider = FakeAuthProvider(),
            stringProvider = FakeStringProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        viewModel.onAction(ForgotPasswordScreenAction.OnBackButtonTap)
        advanceUntilIdle()

        assertEquals(1, navigator.navigateUpCalls)
    }

    @Test
    fun `AuthScreenViewModel OnForgotPasswordTap navigates to ForgotPasswordScreen`() = runTest {
        val navigator = FakeNavigator()
        val viewModel = AuthScreenViewModel(
            navigator = navigator,
            snackbar = FakeSnackbarProvider(),
            authProvider = FakeAuthProvider(),
            stringProvider = FakeStringProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        viewModel.onAction(AuthScreenAction.OnForgotPasswordTap)
        advanceUntilIdle()

        assertEquals(listOf(Destination.ForgotPasswordScreen), navigator.navigatedDestinations)
    }

    private class FakeNavigator : Navigator {
        val navigatedDestinations = mutableListOf<Destination>()
        var navigateUpCalls = 0
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination) {
            navigatedDestinations.add(destination)
        }
        override suspend fun navigateToHomeGraph() {}
        override suspend fun navigateToAuthGraph() {}
        override suspend fun navigateUp() {
            navigateUpCalls++
        }
    }

    private class FakeSnackbarProvider : SnackbarProvider {
        val shownMessages = mutableListOf<String>()
        override val messagesChannel: Flow<SnackbarMessage> = emptyFlow()
        override suspend fun showMessage(message: SnackbarMessage) {
            shownMessages.add(message.message)
        }
    }

    private class FakeAuthProvider(
        private val resetResult: UiState<Boolean> = UiState.Normal(true),
    ) : AuthProvider {
        var lastResetEmail: String? = null
        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun resetPassword(email: String): UiState<Boolean> {
            lastResetEmail = email
            return resetResult
        }
        override suspend fun signOut(): Boolean = true
        override suspend fun isUserExists(): Boolean = true
        override fun getCurrentUserIdFlow(): Flow<String?> = flowOf("user1")
    }

    private class FakeStringProvider : StringProvider {
        override val cannotSignText: String = "Cannot sign in"
        override val alreadyInSquad: String = ""
        override fun squadNotFound(groupId: String): String = ""
        override fun wantToJoinSquad(groupTitle: String): String = ""
        override fun fromToDate(fromDate: String, toDate: String): String = ""
        override val youHaveNoSquad: String = ""
        override val eventSaved: String = ""
        override val eventSaveFailed: String = ""
        override val joinedSquad: String = ""
        override val joinSquadFailed: String = ""
        override val passwordResetEmailSent: String = "Password reset email has been sent"
    }

    private class FakeAnalyticsProvider : AnalyticsProvider {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        override fun trackEvent(event: AnalyticsEvent) {
            trackedEvents.add(event)
        }
        override fun trackScreenView(screenName: String) {}
        override fun setUserId(userId: String?) {}
    }

    private class FakeAppLogger : AppLogger {
        override fun d(tag: String?, message: () -> String) {}
        override fun i(tag: String?, message: () -> String) {}
        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    }
}
