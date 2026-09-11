package com.eysamarin.squadplay.data.contract

import android.os.Bundle
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {

    override fun logEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    null -> {}
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(event.eventName, bundle)
    }

    override fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
