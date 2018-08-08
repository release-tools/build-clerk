package com.gatehill.buildbouncer.api.model

import java.time.ZonedDateTime

/**
 * An analysis event.
 */
data class Event(
    val timestamp: ZonedDateTime,
    val message: String
)
