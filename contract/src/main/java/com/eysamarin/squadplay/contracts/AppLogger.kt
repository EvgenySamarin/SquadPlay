package com.eysamarin.squadplay.contracts

interface AppLogger {
    fun d(tag: String? = null, message: () -> String)
    fun i(tag: String? = null, message: () -> String)
    fun w(tag: String? = null, throwable: Throwable? = null, message: () -> String)
    fun e(tag: String? = null, throwable: Throwable? = null, message: () -> String)
}
