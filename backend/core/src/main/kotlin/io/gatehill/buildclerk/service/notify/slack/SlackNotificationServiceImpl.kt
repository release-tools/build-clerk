package io.gatehill.buildclerk.service.notify.slack

import io.gatehill.buildclerk.api.model.action.PendingAction
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.model.message.UpdatedNotificationMessage
import io.gatehill.buildclerk.api.model.slack.SlackAttachmentField
import io.gatehill.buildclerk.api.model.slack.SlackMessage
import io.gatehill.buildclerk.api.model.slack.SlackMessageAction
import io.gatehill.buildclerk.api.model.slack.SlackMessageAttachment
import io.gatehill.buildclerk.service.notify.StdoutNotificationServiceImpl
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

    override fun notify(
        channelName: String,
        analysis: Analysis,
        color: String
    ) {
        super.notify(channelName, analysis.toString(), color)

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
        val slackMessage = updatedMessage.toSlackMessage()
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
            SlackMessageAction(
                type = "button",
                name = action.name,
                text = action.title,
                value = action.name,
                style = "danger"
            ),
            SlackMessageAction(
                type = "button",
                name = action.name,
                text = "No",
                value = "no"
            )
        )
    )
}
