package com.eysamarin.squadplay.domain.analytics

import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AnalyticsTracker
import com.eysamarin.squadplay.contracts.AppLogger

interface AnalyticsProvider {
    fun trackEvent(event: AnalyticsEvent)
    fun trackScreenView(screenName: String)
    fun setUserId(userId: String?)
}

class AnalyticsProviderImpl(
    private val tracker: AnalyticsTracker,
    private val logger: AppLogger,
) : AnalyticsProvider {

    override fun trackEvent(event: AnalyticsEvent) {
        tracker.logEvent(event)
        logger.i(tag = "Analytics") { "Tracked event '${event.eventName}' with params: ${event.params}" }
    }

    override fun trackScreenView(screenName: String) {
        tracker.logScreenView(screenName)
        logger.i(tag = "Navigation") { "Navigated to screen: $screenName" }
    }

    override fun setUserId(userId: String?) {
        tracker.setUserId(userId)
        logger.i(tag = "Identity") { "Analytics user identity updated: ${userId != null}" }
    }
}
