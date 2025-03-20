package com.eysamarin.squadplay.domain.event

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.EventData
import com.eysamarin.squadplay.models.GameEventUI
import com.eysamarin.squadplay.models.PREVIEW_GAME_EVENTS
import kotlinx.coroutines.flow.Flow

interface EventProvider {
    fun provideEventsUIBy(date: CalendarUI.Date): List<GameEventUI>
    suspend fun saveEventData(event: EventData)
    fun getEventsFlow(groupId: String): Flow<List<EventData>>
}

class EventProviderImpl(
    private val eventRepository: EventRepository,
): EventProvider {

    override fun provideEventsUIBy(date: CalendarUI.Date): List<GameEventUI> {
        return PREVIEW_GAME_EVENTS
    }

    override suspend fun saveEventData(event: EventData) {
        eventRepository.saveEventData(event)
    }

    override fun getEventsFlow(groupId: String): Flow<List<EventData>> {
        return eventRepository.getEventsFlow(groupId)
    }
}
