package com.gatehill.buildbouncer.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackAttachmentAction(
    val name: String,
    val value: String,
    val type: String,
    val text: String,
    val style: String? = null
)
