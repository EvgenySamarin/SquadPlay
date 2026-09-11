package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class EventRepositoryImpl(
    val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val logger: AppLogger,
) : EventRepository {

    override suspend fun saveEventData(event: Event): Boolean = firebaseFirestoreDataSource
        .saveEvent(event)

    override fun getEventsFlow(groupId: String): Flow<List<Event>> = firebaseFirestoreDataSource
        .getEventsFlow(groupId)
        .catch {
            logger.e(tag = "EventRepository", throwable = it) { "Cannot get events flow cause: ${it.message}" }
        }

    override suspend fun deleteEvent(eventID: String): Boolean = firebaseFirestoreDataSource
        .deleteEvent(eventID)
}