package com.eysamarin.squadplay.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Navigator {
    val startDestination: Destination
    val navigationActions: Flow<NavigationAction>

    suspend fun navigate(destination: Destination, navOptions: NavOptionsBuilder.() -> Unit = {})
    suspend fun navigateToHomeGraph()
    suspend fun navigateToAuthGraph()
    suspend fun navigateUp()
}

class DefaultNavigator(override val startDestination: Destination) : Navigator {
    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions = _navigationActions.receiveAsFlow()

    override suspend fun navigate(destination: Destination, navOptions: NavOptionsBuilder.() -> Unit) {
        _navigationActions.send(NavigationAction.Navigate(destination, navOptions))
    }

    override suspend fun navigateToHomeGraph() {
        val action = NavigationAction.Navigate(Destination.HomeGraph) {
            popUpTo(Destination.AuthGraph) { inclusive = true }
        }
        _navigationActions.send(action)
    }

    override suspend fun navigateToAuthGraph() {
        val action = NavigationAction.Navigate(Destination.AuthGraph) {
            popUpTo(Destination.HomeGraph) { inclusive = true }
        }
        _navigationActions.send(action)
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.NavigateUp)
    }
}
