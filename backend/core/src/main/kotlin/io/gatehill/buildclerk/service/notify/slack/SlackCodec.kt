package io.gatehill.buildclerk.service.notify.slack

import io.gatehill.buildclerk.api.model.message.MessageAction
import io.gatehill.buildclerk.api.model.message.MessageAttachment
import io.gatehill.buildclerk.api.model.message.NotificationMessage
import io.gatehill.buildclerk.api.model.message.UpdatedNotificationMessage
import io.gatehill.buildclerk.api.model.slack.SlackMessage
import io.gatehill.buildclerk.api.model.slack.SlackMessageAction
import io.gatehill.buildclerk.api.model.slack.SlackMessageAttachment

/**************************************
 * Convert to Slack messages
 *************************************/

fun NotificationMessage.toSlackMessage() = SlackMessage(
    channel = this.channel,
    text = this.text,
    attachments = this.attachments?.map(MessageAttachment::toSlackMessageAttachment),
    ts = when (this) {
        is UpdatedNotificationMessage -> this.messageId
        else -> null
    }
)

fun MessageAttachment.toSlackMessageAttachment() = SlackMessageAttachment(
    text = this.text,
    color = this.color,
    title = this.title,
    fallback = this.fallback,
    callbackId = this.callbackId,
    actions = this.actions?.map(MessageAction::toSlackAction)
)

fun MessageAction.toSlackAction() = SlackMessageAction(
    name = this.name,
    value = this.value,
    type = this.type,
    text = this.text,
    style = this.style
)

/**************************************
 * Convert from Slack messages
 *************************************/

fun SlackMessageAttachment.toMessageAttachment(
    actions: List<MessageAction>
) = MessageAttachment(
    text = this.text,
    color = this.color,
    title = this.title,
    callbackId = this.callbackId,
    actions = actions
)

fun SlackMessageAction.toMessageAction() = MessageAction(
    name = this.name,
    value = this.value,
    type = this.type,
    text = this.text,
    style = this.style
)
