package com.eysamarin.squadplay.models

sealed class NavAction {
    data class NavigateTo(val route: String) : NavAction()
    object NavigateBack : NavAction()
    object PopToStart : NavAction()
}

sealed class Routes(val route: String) {
    data object Main : Routes(route = "Main")
    data object Auth : Routes(route = "Auth")
}