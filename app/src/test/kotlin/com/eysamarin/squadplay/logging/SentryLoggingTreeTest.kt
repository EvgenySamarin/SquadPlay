package com.eysamarin.squadplay.logging

import android.util.Log
import org.junit.Test
import timber.log.Timber

class SentryLoggingTreeTest {

    @Test
    fun logsAtDifferentLevelsWithoutError() {
        val tree = SentryLoggingTree()
        Timber.plant(tree)

        Timber.tag("TestTag").v("Verbose message")
        Timber.tag("TestTag").d("Debug message")
        Timber.tag("TestTag").i("Info message")
        Timber.tag("TestTag").w(RuntimeException("test exception"), "Warn message")
        Timber.tag("TestTag").e(RuntimeException("test exception"), "Error message")
        Timber.e("Error message without tag")

        Timber.uproot(tree)
    }
}
