package com.eysamarin.squadplay.data.security

import com.eysamarin.squadplay.models.AppError
import com.eysamarin.squadplay.models.AppErrorException
import com.google.firebase.FirebaseException

fun Throwable.isAppCheckAttestationFailure(): Boolean {
    if (this is AppErrorException && error is AppError.SecurityAttestationFailed) return true

    val msg = message.orEmpty()
    val isAppCheckMessage = msg.contains("App attestation failed", ignoreCase = true) ||
            msg.contains("App Check", ignoreCase = true)

    return when {
        this is FirebaseException && isAppCheckMessage -> true
        msg.contains("403", ignoreCase = true) && isAppCheckMessage -> true
        msg.contains("App attestation failed", ignoreCase = true) -> true
        else -> cause?.isAppCheckAttestationFailure() == true
    }
}

fun Throwable.toAppError(): AppError? {
    return if (isAppCheckAttestationFailure()) {
        AppError.SecurityAttestationFailed
    } else {
        null
    }
}
