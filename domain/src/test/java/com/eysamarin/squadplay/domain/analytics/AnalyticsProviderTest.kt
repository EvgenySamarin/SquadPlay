package com.eysamarin.squadplay.domain.analytics

import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AnalyticsTracker
import com.eysamarin.squadplay.contracts.AppLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsProviderTest {

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val loggedEvents = mutableListOf<AnalyticsEvent>()
        val loggedScreens = mutableListOf<String>()
        var recordedUserId: String? = null
        val userProperties = mutableMapOf<String, String?>()

        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }

        override fun logScreenView(screenName: String) {
            loggedScreens.add(screenName)
        }

        override fun setUserId(userId: String?) {
            this.recordedUserId = userId
        }

        override fun setUserProperty(name: String, value: String?) {
            userProperties[name] = value
        }
    }

    private class FakeAppLogger : AppLogger {
        data class LogEntry(val priority: String, val tag: String?, val message: String, val throwable: Throwable?)
        val logs = mutableListOf<LogEntry>()

        override fun d(tag: String?, message: () -> String) {
            logs.add(LogEntry("d", tag, message(), null))
        }

        override fun i(tag: String?, message: () -> String) {
            logs.add(LogEntry("i", tag, message(), null))
        }

        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {
            logs.add(LogEntry("w", tag, message(), throwable))
        }

        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
            logs.add(LogEntry("e", tag, message(), throwable))
        }
    }

    @Test
    fun `trackEvent dispatches event to tracker and logs breadcrumb`() {
        val tracker = FakeAnalyticsTracker()
        val logger = FakeAppLogger()
        val provider = AnalyticsProviderImpl(tracker, logger)

        val event = AnalyticsEvent.JoinGroup(groupId = "squad-42")
        provider.trackEvent(event)

        assertEquals(1, tracker.loggedEvents.size)
        assertEquals(event, tracker.loggedEvents.first())
        assertEquals("join_group", tracker.loggedEvents.first().eventName)
        assertEquals(mapOf("group_id" to "squad-42"), tracker.loggedEvents.first().params)

        assertEquals(1, logger.logs.size)
        assertEquals("i", logger.logs.first().priority)
        assertEquals("Analytics", logger.logs.first().tag)
        assertTrue(logger.logs.first().message.contains("join_group"))
        assertTrue(logger.logs.first().message.contains("squad-42"))
    }

    @Test
    fun `trackScreenView dispatches screen to tracker and logs navigation breadcrumb`() {
        val tracker = FakeAnalyticsTracker()
        val logger = FakeAppLogger()
        val provider = AnalyticsProviderImpl(tracker, logger)

        provider.trackScreenView("HomeScreen")

        assertEquals(listOf("HomeScreen"), tracker.loggedScreens)
        assertEquals(1, logger.logs.size)
        assertEquals("Navigation", logger.logs.first().tag)
        assertTrue(logger.logs.first().message.contains("HomeScreen"))
    }

    @Test
    fun `setUserId updates tracker and logs identity breadcrumb`() {
        val tracker = FakeAnalyticsTracker()
        val logger = FakeAppLogger()
        val provider = AnalyticsProviderImpl(tracker, logger)

        provider.setUserId("user_99")

        assertEquals("user_99", tracker.recordedUserId)
        assertEquals(1, logger.logs.size)
        assertEquals("Identity", logger.logs.first().tag)
        assertTrue(logger.logs.first().message.contains("true"))
    }
}
