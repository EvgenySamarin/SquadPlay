package com.eysamarin.squadplay.data

import com.google.firebase.Timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date

fun Date.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return kotlinx.datetime.Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone)
}

fun LocalDateTime.toTimestamp(timeZone: TimeZone = TimeZone.currentSystemDefault()): Timestamp {
    val instant = toInstant(timeZone)
    return Timestamp(Date(instant.toEpochMilliseconds()))
}
