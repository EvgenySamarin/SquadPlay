package com.eysamarin.squadplay.logging

import android.util.Log
import io.sentry.Sentry
import timber.log.Timber

class SentryLoggingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formattedTag = tag ?: "SquadPlay"
        val formattedMessage = "[$formattedTag] $message"

        when (priority) {
            Log.VERBOSE -> Sentry.logger().trace(formattedMessage)
            Log.DEBUG -> Sentry.logger().debug(formattedMessage)
            Log.INFO -> Sentry.logger().info(formattedMessage)
            Log.WARN -> {
                Sentry.logger().warn(formattedMessage)
                if (t != null) {
                    Sentry.captureException(t)
                }
            }
            Log.ERROR, Log.ASSERT -> {
                Sentry.logger().error(formattedMessage)
                if (t != null) {
                    Sentry.captureException(t)
                }
            }
        }
    }
}
