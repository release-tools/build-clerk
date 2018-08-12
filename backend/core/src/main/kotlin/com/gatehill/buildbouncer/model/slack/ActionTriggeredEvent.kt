package com.gatehill.buildbouncer.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Slack action was triggered.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ActionTriggeredEvent(
        val actions: List<SlackAttachmentAction>?,

        @JsonProperty("original_message")
        val originalMessage: SlackMessage,

        val channel: SlackIdAndName,
        val user: SlackIdAndName,

        @JsonProperty("callback_id")
        val callbackId: String
)
