package com.eysamarin.squadplay.domain.event

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import kotlinx.coroutines.flow.Flow

interface EventProvider {
    suspend fun saveEventData(event: Event): Boolean
    fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>
    suspend fun deleteEvent(eventId: String): Boolean
    suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)
}

class EventProviderImpl(
    private val eventRepository: EventRepository,
): EventProvider {

    override suspend fun saveEventData(event: Event): Boolean = eventRepository
        .saveEventData(event)

    override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = eventRepository
        .getEventsFlow(groupIds)

    override suspend fun deleteEvent(eventId: String): Boolean = eventRepository
        .deleteEvent(eventId)

    override suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus) = eventRepository
        .updateEventResponse(eventId, userId, status)
}
