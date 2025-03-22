package com.eysamarin.squadplay.domain.event

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.PREVIEW_EVENTS
import kotlinx.coroutines.flow.Flow

interface EventProvider {
    fun provideEventsUIBy(date: CalendarUI.Date): List<Event>
    suspend fun saveEventData(event: Event): Boolean
    fun getEventsFlow(groupId: String): Flow<List<Event>>
}

class EventProviderImpl(
    private val eventRepository: EventRepository,
): EventProvider {

    override fun provideEventsUIBy(date: CalendarUI.Date): List<Event> {
        return PREVIEW_EVENTS
    }

    override suspend fun saveEventData(event: Event): Boolean =
        eventRepository.saveEventData(event)

    override fun getEventsFlow(groupId: String): Flow<List<Event>> {
        return eventRepository.getEventsFlow(groupId)
    }
}
