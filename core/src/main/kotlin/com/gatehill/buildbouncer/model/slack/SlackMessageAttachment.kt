package com.gatehill.buildbouncer.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackMessageAttachment(
    val text: String? = null,
    val title: String? = null,
    val color: String? = null,
    val id: Int? = null,
    val fallback: String? = null,
    val actions: List<SlackAttachmentAction>? = null,

    @JsonProperty("callback_id")
    val callbackId: String? = null,

    @JsonProperty("attachment_type")
    val attachmentType: String? = null
)
