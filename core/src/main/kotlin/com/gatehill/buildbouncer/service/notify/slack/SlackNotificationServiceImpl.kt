package com.gatehill.buildbouncer.service.notify.slack

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.PendingAction
import com.gatehill.buildbouncer.service.notify.StdoutNotificationServiceImpl

/**
 * Sends Slack notifications and actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackNotificationServiceImpl(
    private val slackOperationsService: SlackOperationsService
) : StdoutNotificationServiceImpl() {

    override fun notify(channelName: String, message: String, color: String) {
        super.notify(channelName, message, color)

        val content: Map<String, *> = mapOf(
            "channel" to channelName,
            "attachments" to listOf(
                mapOf(
                    "text" to message,
                    "color" to color
                )
            )
        )

        slackOperationsService.sendMessage(content)
    }

    override fun notify(channelName: String, analysis: Analysis, color: String) {
        super.notify(channelName, analysis, color)

        if (analysis.actionSet.actions.isEmpty()) {
            return
        }

        val content: Map<String, *> = mapOf(
            "text" to analysis.describeEvents(),
            "channel" to channelName,
            "attachments" to analysis.actionSet.actions.map { action ->
                buildMessageAction(analysis.actionSet.id, action, color)
            }
        )

        slackOperationsService.sendMessage(content)
    }

    private fun buildMessageAction(
        actionSetId: String,
        action: PendingAction, color: String
    ): Map<String, Any> = mapOf(
        "fallback" to "Do you want to ${action.describe()}?",
        "title" to "Do you want to ${action.describe()}?",
        "callback_id" to actionSetId,
        "color" to color,
        "attachment_type" to "default",
        "actions" to listOf(
            mapOf(
                "name" to action.name,
                "text" to action.title,
                "style" to "danger",
                "type" to "button",
                "value" to action.name
            ),
            mapOf(
                "name" to "no",
                "text" to "No",
                "type" to "button",
                "value" to "bad"
            )
        )
    )
}
