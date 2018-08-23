package com.gatehill.buildclerk.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackMessageAction(
        val name: String? = null,
        val value: String? = null,
        val type: String? = null,
        val text: String? = null,
        val style: String? = null
)
