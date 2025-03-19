package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.EventRepository
import com.eysamarin.squadplay.data.datasource.FirebaseFirestoreDataSource
import com.eysamarin.squadplay.models.EventData

class EventRepositoryImpl(
    val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
) : EventRepository {

    override suspend fun saveEventData(event: EventData) {
        firebaseFirestoreDataSource.saveEvent(event)
    }
}