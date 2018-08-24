package com.gatehill.buildclerk.api.model.message

/**
 * A simple message.
 */
open class NotificationMessage(
    val channel: String,
    val text: String? = null,
    val attachments: List<MessageAttachment>? = null
)
