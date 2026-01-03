package com.eysamarin.squadplay.navigation

import androidx.navigation.NavOptionsBuilder
import com.eysamarin.squadplay.models.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

sealed interface NavigationAction {
    data class Navigate(
        val destination: Destination,
        val navOptions: NavOptionsBuilder.() -> Unit = {},
    ) : NavigationAction

    object NavigateUp : NavigationAction
}

sealed interface Destination {

    @Serializable
    data object AuthGraph: Destination
    @Serializable
    object AuthScreen: Destination
    @Serializable
    object RegistrationScreen: Destination

    @Serializable
    data object HomeGraph: Destination
    @Serializable
    data class HomeScreen(val inviteGroupID: String? = null): Destination
    @Serializable
    object ProfileScreen: Destination
    @Serializable
    data class NewEventScreen(
        val selectedDate: Date,
        val yearMonth: String,
    ): Destination {
        object CustomNavType {
            val DateType = serializableNavType(serializer<Date>())
        }
    }
}
