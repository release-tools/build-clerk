package com.gatehill.buildclerk.api.model.analysis

import java.time.ZonedDateTime

/**
 * An timestamped analysis event.
 */
data class AnalysisEvent(
    val timestamp: ZonedDateTime,
    val message: String
)
