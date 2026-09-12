package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class EventRepositoryImpl(
    val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val logger: AppLogger,
) : EventRepository {

    override suspend fun saveEventData(event: Event): Boolean = firebaseFirestoreDataSource
        .saveEvent(event)

    override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = firebaseFirestoreDataSource
        .getEventsFlow(groupIds)
        .catch {
            logger.e(tag = "EventRepository", throwable = it) { "Cannot get events flow cause: ${it.message}" }
        }

    override suspend fun deleteEvent(eventID: String): Boolean = firebaseFirestoreDataSource
        .deleteEvent(eventID)

    override suspend fun updateEventResponse(
        eventId: String,
        userId: String,
        status: EventResponseStatus,
    ) {
        try {
            firebaseFirestoreDataSource.updateEventResponse(eventId, userId, status)
        } catch (e: Exception) {
            logger.e(tag = "EventRepository", throwable = e) { "Cannot update event response cause: ${e.message}" }
        }
    }
}