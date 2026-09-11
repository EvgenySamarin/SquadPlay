package com.eysamarin.squadplay.data.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.INFO
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formattedTag = tag ?: "SquadPlay"
        val crashlytics = FirebaseCrashlytics.getInstance()

        when (priority) {
            Log.INFO, Log.WARN -> {
                crashlytics.log("[$formattedTag] $message")
            }
            Log.ERROR -> {
                crashlytics.log("[$formattedTag] $message")
                if (t != null) {
                    crashlytics.recordException(t)
                } else {
                    crashlytics.recordException(Exception("Non-fatal error: [$formattedTag] $message"))
                }
            }
        }
    }
}
