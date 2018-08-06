package com.gatehill.buildbouncer.service.notify.slack

import com.gatehill.buildbouncer.config.Settings
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

    override fun notify(analysis: Analysis) {
        super.notify(analysis)

        if (analysis.actionSet.actions.isEmpty()) {
            return
        }

        val content: Map<String, *> = mapOf(
                "text" to analysis.describeEvents(),
                "channel" to Settings.Slack.channel,
                "attachments" to analysis.actionSet.actions.map { action ->
                    buildMessageAction(analysis.actionSet.id, action)
                }
        )

        slackOperationsService.sendMessage(content)
    }

    private fun buildMessageAction(actionSetId: String, action: PendingAction): Map<String, Any> = mapOf(
            "fallback" to "Do you want to ${action.describe()}?",
            "title" to "Do you want to ${action.describe()}?",
            "callback_id" to actionSetId,
            "color" to "#3AA3E3",
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
