package com.eysamarin.squadplay.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Navigator {
    val navigationActions: Flow<NavigationAction>

    suspend fun navigate(destination: Destination)
    suspend fun navigateToHomeGraph()
    suspend fun navigateToAuthGraph()
    suspend fun navigateUp()
}

class DefaultNavigator : Navigator {
    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions = _navigationActions.receiveAsFlow()

    override suspend fun navigate(destination: Destination) {
        _navigationActions.send(NavigationAction.Navigate(destination))
    }

    override suspend fun navigateToHomeGraph() {
        _navigationActions.send(NavigationAction.Navigate(Destination.HomeGraph))
    }

    override suspend fun navigateToAuthGraph() {
        _navigationActions.send(NavigationAction.Navigate(Destination.AuthGraph))
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.NavigateUp)
    }
}
