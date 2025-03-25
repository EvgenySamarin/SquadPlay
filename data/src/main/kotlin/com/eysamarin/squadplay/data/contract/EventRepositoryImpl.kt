package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.Event
import kotlinx.coroutines.flow.Flow

class EventRepositoryImpl(
    val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
) : EventRepository {

    override suspend fun saveEventData(event: Event): Boolean = firebaseFirestoreDataSource
        .saveEvent(event)

    override fun getEventsFlow(groupId: String): Flow<List<Event>> = firebaseFirestoreDataSource
        .getEventsFlow(groupId)

    override suspend fun deleteEvent(eventID: String): Boolean = firebaseFirestoreDataSource
        .deleteEvent(eventID)
}