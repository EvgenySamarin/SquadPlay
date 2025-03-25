package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun saveEventData(event: Event): Boolean
    fun getEventsFlow(groupId: String): Flow<List<Event>>
    suspend fun deleteEvent(eventID: String): Boolean
}