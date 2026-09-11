package com.eysamarin.squadplay.data.logging

import com.eysamarin.squadplay.contracts.AppLogger
import timber.log.Timber

class TimberAppLogger : AppLogger {

    override fun d(tag: String?, message: () -> String) {
        val tree = tag?.let { Timber.tag(it) } ?: Timber.asTree()
        tree.d(message())
    }

    override fun i(tag: String?, message: () -> String) {
        val tree = tag?.let { Timber.tag(it) } ?: Timber.asTree()
        tree.i(message())
    }

    override fun w(tag: String?, throwable: Throwable?, message: () -> String) {
        val tree = tag?.let { Timber.tag(it) } ?: Timber.asTree()
        if (throwable != null) {
            tree.w(throwable, message())
        } else {
            tree.w(message())
        }
    }

    override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
        val tree = tag?.let { Timber.tag(it) } ?: Timber.asTree()
        if (throwable != null) {
            tree.e(throwable, message())
        } else {
            tree.e(message())
        }
    }
}
