package com.eysamarin.squadplay.domain.event

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.models.Event
import kotlinx.coroutines.flow.Flow

interface EventProvider {
    suspend fun saveEventData(event: Event): Boolean
    fun getEventsFlow(groupId: String): Flow<List<Event>>
    suspend fun deleteEvent(eventId: String): Boolean
}

class EventProviderImpl(
    private val eventRepository: EventRepository,
): EventProvider {

    override suspend fun saveEventData(event: Event): Boolean = eventRepository
        .saveEventData(event)

    override fun getEventsFlow(groupId: String): Flow<List<Event>> = eventRepository
        .getEventsFlow(groupId)

    override suspend fun deleteEvent(eventId: String): Boolean = eventRepository
        .deleteEvent(eventId)
}
