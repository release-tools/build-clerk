package com.gatehill.buildclerk.api.model

/**
 * A simple message.
 */
open class NotificationMessage(
        val channel: String,
        val text: String? = null,
        val attachments: List<MessageAttachment>? = null
)

/**
 * An update to an existing message.
 */
class UpdatedNotificationMessage(
        /**
         * Maps to 'ts' in Slack.
         */
        val messageId: String,
        channel: String,
        text: String?,
        attachments: List<MessageAttachment>?
) : NotificationMessage(channel, text, attachments)

/**
 * A basic textual attachment.
 */
data class MessageAttachment(
        val text: String? = null,
        val color: String? = null,
        val title: String? = null,
        val fallback: String? = null
)
