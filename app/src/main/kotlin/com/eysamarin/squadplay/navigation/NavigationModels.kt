package com.eysamarin.squadplay.navigation

import androidx.navigation3.runtime.NavKey
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.EventResponseStatus
import kotlinx.serialization.Serializable

sealed interface NavigationAction {
    data class Navigate(
        val destination: Destination,
    ) : NavigationAction

    data object NavigateUp : NavigationAction
}

@Serializable
sealed interface Destination : NavKey {
    val screenName: String? get() = null

    @Serializable
    data object AuthGraph : Destination

    @Serializable
    data object AuthScreen : Destination {
        const val SCREEN_NAME = "AuthScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data object RegistrationScreen : Destination {
        const val SCREEN_NAME = "RegistrationScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data object HomeGraph : Destination

    @Serializable
    data object HomeScreen : Destination {
        const val SCREEN_NAME = "HomeScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data object ProfileScreen : Destination {
        const val SCREEN_NAME = "ProfileScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data object SettingsScreen : Destination {
        const val SCREEN_NAME = "SettingsScreen"
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data class NewEventScreen(
        val selectedDate: Date,
        val yearMonth: String,
    ) : Destination {
        companion object {
            const val SCREEN_NAME = "NewEventScreen"
        }
        override val screenName: String get() = SCREEN_NAME
    }

    @Serializable
    data class EventDetailsScreen(
        val eventId: String,
        val title: String,
        val date: String,
        val imageUrl: String? = null,
        val isYourEvent: Boolean = false,
        val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
        val groupId: String = "",
        val isObsolete: Boolean = false,
    ) : Destination {
        companion object {
            const val SCREEN_NAME = "EventDetailsScreen"
        }
        override val screenName: String get() = SCREEN_NAME
    }
}
