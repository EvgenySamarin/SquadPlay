package com.eysamarin.squadplay.contracts

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun logScreenView(screenName: String)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}
