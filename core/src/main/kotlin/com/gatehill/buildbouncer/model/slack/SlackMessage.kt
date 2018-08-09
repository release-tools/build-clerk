package com.gatehill.buildbouncer.model.slack

data class SlackMessage(
    val channel: String,
    val text: String? = null,
    val attachments: List<SlackMessageAttachment>? = null
)
