package io.gatehill.buildclerk.model.message

/**
 * Basic wrapper. See docs: https://api.slack.com/events-api#receiving_events
 */
data class EventApiEventWrapper(
        val type: String,
        val event: Map<String, *>
)
