package io.gatehill.buildclerk.api.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackAttachmentField(
    val title: String? = null,
    val value: String? = null,
    val short: Boolean? = null
)
