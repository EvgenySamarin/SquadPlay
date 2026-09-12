package com.eysamarin.squadplay.data.entity

import com.eysamarin.squadplay.data.toLocalDateTime
import com.eysamarin.squadplay.data.toTimestamp
import com.eysamarin.squadplay.models.Event
import com.google.firebase.Timestamp
import kotlinx.datetime.LocalDateTime

data class EventEntity(
    val id: String = "",
    val creatorId: String = "",
    val groupId: String = "",
    val title: String = "",
    val eventIconUrl: String? = null,
    val dateFrom: Timestamp? = null,
    val dateTo: Timestamp? = null,
    val responses: Map<String, String> = emptyMap(),
) {
    fun toDomain(): Event = Event(
        uid = id,
        creatorId = creatorId,
        groupId = groupId,
        title = title,
        eventIconUrl = eventIconUrl,
        fromDateTime = dateFrom?.toDate()?.toLocalDateTime() ?: LocalDateTime(1970, 1, 1, 0, 0),
        toDateTime = dateTo?.toDate()?.toLocalDateTime() ?: LocalDateTime(1970, 1, 1, 0, 0),
        responses = responses,
    )

    companion object {
        fun fromDomain(event: Event): EventEntity = EventEntity(
            id = event.uid,
            creatorId = event.creatorId,
            groupId = event.groupId,
            title = event.title,
            eventIconUrl = event.eventIconUrl,
            dateFrom = event.fromDateTime.toTimestamp(),
            dateTo = event.toDateTime.toTimestamp(),
            responses = event.responses,
        )
    }
}
