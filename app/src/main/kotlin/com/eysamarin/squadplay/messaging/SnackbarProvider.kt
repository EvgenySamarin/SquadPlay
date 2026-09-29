package com.eysamarin.squadplay.messaging

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: (() -> Unit)? = null,
)

interface SnackbarProvider {
    val messagesChannel: Flow<SnackbarMessage>

    suspend fun showMessage(message: String) {
        showMessage(SnackbarMessage(message = message))
    }

    suspend fun showMessage(
        message: String,
        actionLabel: String?,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null,
    ) {
        showMessage(
            SnackbarMessage(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
                onAction = onAction,
            )
        )
    }

    suspend fun showMessage(message: SnackbarMessage)
}

class SnackbarProviderImpl : SnackbarProvider {
    private val _messagesChannel = Channel<SnackbarMessage>(Channel.RENDEZVOUS)
    override val messagesChannel: Flow<SnackbarMessage> = _messagesChannel.receiveAsFlow()

    override suspend fun showMessage(message: SnackbarMessage) {
        _messagesChannel.send(message)
    }
}
