package io.gatehill.buildclerk.model.message

/**
 * Verifies ownership of an Events API Request URL.
 * See https://api.slack.com/events/url_verification
 */
data class UrlVerificationEvent(
    val token: String,
    val challenge: String,
    val type: String
)
