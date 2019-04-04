package io.gatehill.buildclerk.model.message

/**
 * See docs: https://api.slack.com/events-api#receiving_events
 */
data class EventApiEvent(
    val type: String
)
