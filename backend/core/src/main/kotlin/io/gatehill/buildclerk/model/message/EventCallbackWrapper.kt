package io.gatehill.buildclerk.model.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Basic wrapper. See docs: https://api.slack.com/events-api#event_type_structure
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EventCallbackWrapper(
    val type: String,
    val event: Map<String, *>
)
