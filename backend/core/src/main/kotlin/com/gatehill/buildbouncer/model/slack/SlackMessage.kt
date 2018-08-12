package com.gatehill.buildbouncer.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackMessage(
        val channel: String? = null,
        val text: String? = null,

        /**
         * Used when updating messages.
         */
        val ts: String? = null,
        val attachments: List<SlackMessageAttachment>? = null
)
