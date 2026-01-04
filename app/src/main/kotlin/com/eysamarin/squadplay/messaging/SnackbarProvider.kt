package com.eysamarin.squadplay.messaging

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface SnackbarProvider {
    val messagesChannel: Flow<String>

    suspend fun showMessage(message: String)
}

class SnackbarProviderImpl : SnackbarProvider {
    private val _messagesChannel = Channel<String>(Channel.RENDEZVOUS)
    override val messagesChannel = _messagesChannel.receiveAsFlow()

    override suspend fun showMessage(message: String) {
        _messagesChannel.send(message)
    }
}
