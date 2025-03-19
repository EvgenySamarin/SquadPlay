package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.EventData

fun interface EventRepository {
    suspend fun saveEventData(event: EventData)
}