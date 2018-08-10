package com.gatehill.buildbouncer.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackMessage(
        val channel: String? = null,
        val text: String? = null,
        val attachments: List<SlackMessageAttachment>? = null
)
