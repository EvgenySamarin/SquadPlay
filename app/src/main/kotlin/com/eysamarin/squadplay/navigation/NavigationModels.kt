package com.eysamarin.squadplay.navigation

import androidx.navigation.NavOptionsBuilder
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.EventResponseStatus
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
    val screenName: String? get() = null

    @Serializable
    data object AuthGraph: Destination

    @Serializable
    object AuthScreen: Destination {
        const val SCREEN_NAME = "AuthScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    object RegistrationScreen: Destination {
        const val SCREEN_NAME = "RegistrationScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data object HomeGraph: Destination

    @Serializable
    data class HomeScreen(val inviteGroupID: String? = null): Destination {
        companion object {
            const val SCREEN_NAME = "HomeScreen"
        }
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    object ProfileScreen: Destination {
        const val SCREEN_NAME = "ProfileScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    object SettingsScreen : Destination {
        const val SCREEN_NAME = "SettingsScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data class NewEventScreen(
        val selectedDate: Date,
        val yearMonth: String,
    ): Destination {
        companion object {
            const val SCREEN_NAME = "NewEventScreen"
        }
        override val screenName: String get() = SCREEN_NAME

        object CustomNavType {
            val DateType = serializableNavType(serializer<Date>())
        }
    }

    @Serializable
    data class EventDetailsScreen(
        val eventId: String,
        val title: String,
        val date: String,
        val imageUrl: String? = null,
        val isYourEvent: Boolean = false,
        val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
    ) : Destination {
        companion object {
            const val SCREEN_NAME = "EventDetailsScreen"
        }
        override val screenName: String get() = SCREEN_NAME
    }
}
