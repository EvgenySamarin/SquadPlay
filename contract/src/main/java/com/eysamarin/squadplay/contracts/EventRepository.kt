package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    @Throws(AppErrorException::class)
    suspend fun saveEventData(event: Event): Boolean

    fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>

    @Throws(AppErrorException::class)
    suspend fun deleteEvent(eventID: String): Boolean

    @Throws(AppErrorException::class)
    suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)
}