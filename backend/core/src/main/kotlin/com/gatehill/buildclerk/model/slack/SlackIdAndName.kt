package com.gatehill.buildclerk.model.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Used for a variety of objects, such as channels and users.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackIdAndName(
        val id: String,
        val name: String
)