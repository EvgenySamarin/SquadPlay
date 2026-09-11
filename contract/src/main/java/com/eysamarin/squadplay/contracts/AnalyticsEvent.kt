package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Date

sealed interface AnalyticsEvent {
    val eventName: String
    val params: Map<String, Any?> get() = emptyMap()

    data class GroupCreated(val groupId: String) : AnalyticsEvent {
        override val eventName: String = "group_created"
        override val params: Map<String, Any?> = mapOf("group_id" to groupId)
    }

    data class JoinGroup(val groupId: String) : AnalyticsEvent {
        override val eventName: String = "join_group"
        override val params: Map<String, Any?> = mapOf("group_id" to groupId)
    }

    data object SignInGoogleClicked : AnalyticsEvent {
        override val eventName: String = "sign_in_google_clicked"
    }

    data object SignInSuccess : AnalyticsEvent {
        override val eventName: String = "sign_in_success"
    }

    data object SignOut : AnalyticsEvent {
        override val eventName: String = "sign_out"
    }

    data object CreateEventClicked : AnalyticsEvent {
        override val eventName: String = "create_event_clicked"
    }

    data class EventSaved(val eventId: String? = null) : AnalyticsEvent {
        override val eventName: String = "event_saved"
        override val params: Map<String, Any?>
            get() = eventId?.let { mapOf("event_id" to it) } ?: emptyMap()
    }

    data class EventDeleted(val eventId: String? = null) : AnalyticsEvent {
        override val eventName: String = "event_deleted"
        override val params: Map<String, Any?>
            get() = eventId?.let { mapOf("event_id" to it) } ?: emptyMap()
    }

    data class CalendarDateSelected(val date: Date) : AnalyticsEvent {
        override val eventName: String = "calendar_date_selected"
        override val params: Map<String, Any?> = mapOf("date" to date.toAnalyticsString())

        private fun Date.toAnalyticsString(): String {
            return if (dayOfMonth != null && monthNumber != null) {
                "%02d-%02d".format(monthNumber, dayOfMonth)
            } else {
                "none"
            }
        }
    }

    data object InviteShared : AnalyticsEvent {
        override val eventName: String = "invite_shared"
    }

    data object OssLicensesClicked : AnalyticsEvent {
        override val eventName: String = "oss_licenses_clicked"
    }

    data class Custom(
        override val eventName: String,
        override val params: Map<String, Any?> = emptyMap()
    ) : AnalyticsEvent
}
