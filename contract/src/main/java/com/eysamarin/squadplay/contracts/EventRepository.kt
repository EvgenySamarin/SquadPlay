package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.EventData
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun saveEventData(event: EventData)
    fun getEventsFlow(groupId: String): Flow<List<EventData>>
}