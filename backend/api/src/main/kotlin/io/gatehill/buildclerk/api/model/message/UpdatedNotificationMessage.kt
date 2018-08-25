package io.gatehill.buildclerk.api.model.message

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
