package com.eysamarin.squadplay.models

sealed class NavAction {
    data class NavigateTo(val route: String) : NavAction()
    object NavigateBack : NavAction()
    object PopToStart : NavAction()
}

sealed class Route(val route: String) {
    data object Main : Route(route = "Main")
    data object Auth : Route(route = "Auth")
    data object Profile : Route(route = "Profile")
}