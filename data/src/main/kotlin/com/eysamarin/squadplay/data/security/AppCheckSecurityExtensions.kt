package com.eysamarin.squadplay.data.security

import com.eysamarin.squadplay.models.AppError
import com.eysamarin.squadplay.models.AppErrorException

fun Throwable.isAppCheckAttestationFailure(): Boolean {
    if (this is AppErrorException && error is AppError.SecurityAttestationFailed) return true
    val msg = message.orEmpty()
    return msg.contains("App attestation failed", ignoreCase = true) ||
            (msg.contains("403", ignoreCase = true) && msg.contains("App Check", ignoreCase = true)) ||
            (cause?.isAppCheckAttestationFailure() == true)
}

fun Throwable.toAppError(): AppError? {
    return if (isAppCheckAttestationFailure()) {
        AppError.SecurityAttestationFailed
    } else {
        null
    }
}
