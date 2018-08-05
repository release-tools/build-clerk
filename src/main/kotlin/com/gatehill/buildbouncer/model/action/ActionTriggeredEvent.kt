package com.gatehill.buildbouncer.model.action

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Slack action was triggered.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ActionTriggeredEvent(
        val actions: List<SlackAction>?,

        val channel: SlackIdAndName,
        val user: SlackIdAndName,

        @JsonProperty("callback_id")
        val callbackId: String,

        /**
         * Can be used to reply to the message that triggered the action.
         */
        @JsonProperty("response_url")
        val responseUrl: String?
)

/**
 * Used for a variety of objects, such as channels and users.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackIdAndName(
        val id: String,
        val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackAction(
        val name: String,
        val value: String,
        val type: String
)
