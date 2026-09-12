package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun saveEventData(event: Event): Boolean
    fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>
    suspend fun deleteEvent(eventID: String): Boolean
    suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)
}