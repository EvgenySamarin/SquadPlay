package com.eysamarin.squadplay.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.eysamarin.squadplay.BuildConfig

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun String.hideSensitiveInLogs(): String = if (BuildConfig.DEBUG) this else take(5) + "..."