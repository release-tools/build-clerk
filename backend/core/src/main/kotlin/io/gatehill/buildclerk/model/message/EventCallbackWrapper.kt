package io.gatehill.buildclerk.model.message

/**
 * Basic wrapper. See docs: https://api.slack.com/events-api#event_type_structure
 */
data class EventCallbackWrapper(
    val type: String,
    val event: Map<String, *>
)
