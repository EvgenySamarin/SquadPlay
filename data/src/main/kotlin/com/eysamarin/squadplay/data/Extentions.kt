package com.eysamarin.squadplay.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Instant.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
    // Convert the Instant to a ZonedDateTime using the specified ZoneId
    return atZone(zoneId).toLocalDateTime()
}