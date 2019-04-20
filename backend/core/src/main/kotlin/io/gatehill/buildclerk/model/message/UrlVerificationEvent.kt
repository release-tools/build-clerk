package io.gatehill.buildclerk.model.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Verifies ownership of an Events API Request URL.
 * See https://api.slack.com/events/url_verification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UrlVerificationEvent(
    val token: String,
    val challenge: String,
    val type: String
)
