package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.EventData
import kotlinx.coroutines.flow.Flow

class EventRepositoryImpl(
    val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
) : EventRepository {

    override suspend fun saveEventData(event: EventData): Boolean =
        firebaseFirestoreDataSource.saveEvent(event)

    override fun getEventsFlow(groupId: String): Flow<List<EventData>> {
        return firebaseFirestoreDataSource.getEventsFlow(groupId)
    }
}