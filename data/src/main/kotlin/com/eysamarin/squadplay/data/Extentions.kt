package com.eysamarin.squadplay.data

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
    // Convert the Instant to a ZonedDateTime using the specified ZoneId
    return toInstant().atZone(zoneId).toLocalDateTime()
}

fun LocalDateTime.toTimestamp(zoneId: ZoneId = ZoneId.systemDefault()): Timestamp {
    // Convert the LocalDateTime to a ZonedDateTime using the specified ZoneId
    val instant = this.atZone(zoneId).toInstant()
    return Timestamp(instant)
}