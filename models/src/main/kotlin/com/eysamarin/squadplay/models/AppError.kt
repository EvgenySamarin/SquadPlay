package com.eysamarin.squadplay.models

sealed interface AppError {
    data object SecurityAttestationFailed : AppError
}

class AppErrorException(
    val error: AppError,
    override val message: String? = when (error) {
        AppError.SecurityAttestationFailed -> "App attestation failed"
    },
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
