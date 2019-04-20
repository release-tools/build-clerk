package io.gatehill.buildclerk.model.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * See docs: https://api.slack.com/events-api#receiving_events
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EventApiEvent(
    val type: String
)
