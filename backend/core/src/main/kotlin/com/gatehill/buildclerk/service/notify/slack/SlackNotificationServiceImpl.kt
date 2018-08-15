package com.gatehill.buildclerk.service.notify.slack

import com.gatehill.buildclerk.api.model.Analysis
import com.gatehill.buildclerk.api.model.NotificationMessage
import com.gatehill.buildclerk.api.model.UpdatedNotificationMessage
import com.gatehill.buildclerk.api.model.action.PendingAction
import com.gatehill.buildclerk.model.slack.SlackAttachmentAction
import com.gatehill.buildclerk.model.slack.SlackAttachmentField
import com.gatehill.buildclerk.model.slack.SlackMessage
import com.gatehill.buildclerk.model.slack.SlackMessageAttachment
import com.gatehill.buildclerk.service.notify.StdoutNotificationServiceImpl
import javax.inject.Inject

/**
 * Sends Slack notifications and actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackNotificationServiceImpl @Inject constructor(
        private val slackOperationsService: SlackOperationsService
) : StdoutNotificationServiceImpl() {

    override fun notify(channelName: String, message: String, color: String) {
        super.notify(channelName, message, color)

        val content = SlackMessage(
                channel = channelName,
                attachments = listOf(
                        SlackMessageAttachment(
                                text = message,
                                color = color
                        )
                )
        )

        slackOperationsService.sendMessage(content)
    }

    override fun notify(channelName: String, analysis: Analysis, color: String) {
        super.notify(channelName, analysis.toString(), color)

        if (analysis.actionSet.actions.isEmpty()) {
            return
        }

        val content = SlackMessage(
                channel = channelName,
                attachments = mutableListOf<SlackMessageAttachment>().apply {
                    this += buildAnalysisAttachment(analysis, color)
                    this += analysis.actionSet.actions.map { action ->
                        buildMessageAttachment(analysis.actionSet.id, action, color)
                    }
                }
        )

        slackOperationsService.sendMessage(content)
    }

    override fun updateMessage(updatedMessage: UpdatedNotificationMessage) {
        val slackMessage = convertToSlackMessage(updatedMessage)
        slackOperationsService.updateMessage(slackMessage)
    }

    private fun buildAnalysisAttachment(
            analysis: Analysis,
            color: String
    ): SlackMessageAttachment {
        val fields = mutableListOf(
                SlackAttachmentField(
                        title = "Branch",
                        value = analysis.branch,
                        short = true
                )
        )

        analysis.user?.let {
            fields += SlackAttachmentField(
                    title = "User",
                    value = analysis.user,
                    short = true
            )
        }

        return SlackMessageAttachment(
                fallback = analysis.describeEvents(),
                title = analysis.name,
                titleLink = analysis.url,
                text = analysis.describeEvents(),
                color = color,
                attachmentType = "default",
                fields = fields
        )
    }

    private fun buildMessageAttachment(
            actionSetId: String,
            action: PendingAction,
            color: String
    ) = SlackMessageAttachment(
            fallback = "Do you want to ${action.describe()}?",
            title = "Do you want to ${action.describe()}?",
            callbackId = actionSetId,
            color = color,
            attachmentType = "default",
            actions = listOf(
                    SlackAttachmentAction(
                            type = "button",
                            name = action.name,
                            text = action.title,
                            value = action.name,
                            style = "danger"
                    ),
                    SlackAttachmentAction(
                            type = "button",
                            name = action.name,
                            text = "No",
                            value = "no"
                    )
            )
    )

    private fun convertToSlackMessage(updatedMessage: NotificationMessage) = SlackMessage(
            channel = updatedMessage.channel,
            text = updatedMessage.text,
            attachments = updatedMessage.attachments?.map { attachment ->
                SlackMessageAttachment(
                        text = attachment.text,
                        color = attachment.color,
                        title = attachment.title,
                        fallback = attachment.fallback
                )
            },
            ts = when (updatedMessage) {
                is UpdatedNotificationMessage -> updatedMessage.messageId
                else -> null
            }
    )
}
