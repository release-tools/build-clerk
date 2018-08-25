package io.gatehill.buildclerk.api.model.message

/**
 * A basic textual attachment.
 */
data class MessageAttachment(
    val text: String? = null,
    val color: String? = null,
    val title: String? = null,
    val fallback: String? = null,
    val callbackId: String? = null,
    val actions: List<MessageAction>? = null
)
