package com.eysamarin.squadplay.data

import com.google.firebase.Timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import kotlin.time.Instant

fun Date.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return Instant.fromEpochMilliseconds(this.time).toLocalDateTime(timeZone)
}

fun LocalDateTime.toTimestamp(timeZone: TimeZone = TimeZone.currentSystemDefault()): Timestamp {
    val instant = toInstant(timeZone)
    return Timestamp(Date(instant.toEpochMilliseconds()))
}
