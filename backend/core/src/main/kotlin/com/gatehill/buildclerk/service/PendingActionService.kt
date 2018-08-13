package com.gatehill.buildclerk.service

import com.gatehill.buildclerk.api.model.MessageAttachment
import com.gatehill.buildclerk.api.model.UpdatedNotificationMessage
import com.gatehill.buildclerk.api.model.action.LockBranchAction
import com.gatehill.buildclerk.api.model.action.PendingAction
import com.gatehill.buildclerk.api.model.action.PendingActionSet
import com.gatehill.buildclerk.api.model.action.RebuildBranchAction
import com.gatehill.buildclerk.api.model.action.RevertAction
import com.gatehill.buildclerk.api.service.BuildRunnerService
import com.gatehill.buildclerk.api.service.NotificationService
import com.gatehill.buildclerk.model.slack.ActionTriggeredEvent
import com.gatehill.buildclerk.model.slack.SlackAttachmentAction
import com.gatehill.buildclerk.model.slack.SlackMessageAttachment
import com.gatehill.buildclerk.service.scm.ScmService
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Completes pending actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class PendingActionService @Inject constructor(
        private val scmService: ScmService,
        private val buildRunnerService: BuildRunnerService,
        private val notificationService: NotificationService
) {
    private val logger: Logger = LogManager.getLogger(PendingActionService::class.java)
    private val pending = mutableMapOf<String, PendingActionSet>()

    fun enqueue(actionSet: PendingActionSet) {
        logger.info("Enqueuing ${actionSet.actions.size} pending actions: ${actionSet.actions}")
        pending[actionSet.id] = actionSet
    }

    fun handleAsync(event: ActionTriggeredEvent) {
        @Suppress("DeferredResultUnused")
        async {
            try {
                handle(event)
            } catch (e: Exception) {
                logger.error("Error handling action trigger with callback ID: ${event.callbackId}", e)
            }
        }
    }

    private fun handle(event: ActionTriggeredEvent) {
        logger.info("Handling action trigger with callback ID: ${event.callbackId}")

        val attachments: MutableList<MessageAttachment> = stripActions(event.originalMessage.attachments)

        event.actions?.let { actions ->
            val actionSetId = event.callbackId

            pending.remove(actionSetId)?.let { actionSet ->
                logger.debug("Found pending action set with ID: $actionSetId [${actionSet.actions.size} actions]")
                actions.forEach { action ->
                    val emoji = if (resolve(action, actionSet)) "white_check_mark" else "negative_squared_cross_mark"

                    // indicate outcome
                    attachments += MessageAttachment(
                            text = ":$emoji: <@${event.user.id}> selected '${action.value}'"
                    )
                }

            } ?: logger.warn("No pending action set found with ID: $actionSetId")

        } ?: logger.warn("No actions found in event: $event")

        // update the original message
        updateOriginalMessage(event, attachments)
    }

    /**
     * Remove buttons from message attachments by creating new attachments without actions.
     */
    private fun stripActions(
            attachments: List<SlackMessageAttachment>?
    ): MutableList<MessageAttachment> = attachments
            ?.map(this::convertSlackAttachmentToSimpleAttachment)
            ?.toMutableList() ?: mutableListOf()

    private fun convertSlackAttachmentToSimpleAttachment(
            slackAttachment: SlackMessageAttachment
    ) = MessageAttachment(
            text = slackAttachment.text,
            color = slackAttachment.color,
            title = slackAttachment.title
    )

    private fun updateOriginalMessage(
            event: ActionTriggeredEvent,
            attachments: MutableList<MessageAttachment>
    ) {
        event.originalMessage.ts?.let {
            val updatedMessage = UpdatedNotificationMessage(
                    messageId = event.originalMessage.ts,
                    channel = event.channel.id,
                    text = event.originalMessage.text,
                    attachments = attachments
            )
            notificationService.updateMessage(updatedMessage)

        } ?: logger.warn("Cannot update original message will callback ID: ${event.callbackId}, as there was no message timestamp")
    }

    private fun resolve(action: SlackAttachmentAction, actionSet: PendingActionSet): Boolean {
        logger.debug("Attempting to resolve pending action: $action")

        actionSet.actions.find { it.name == action.name }?.let { pendingAction ->
            when (action.value) {
                pendingAction.name -> {
                    executePendingAction(pendingAction)
                    return true
                }
                else -> logger.info("Discarding pending action: $pendingAction [actionValue: ${action.value}]")
            }

        } ?: logger.warn("No such action '${action.name}' in pending action set: ${actionSet.id}")

        return false
    }

    private fun executePendingAction(pendingAction: PendingAction) {
        logger.info("Executing pending action: $pendingAction")
        when (pendingAction) {
            is RevertAction -> scmService.revertCommit(pendingAction.commit, pendingAction.branch)
            is RebuildBranchAction -> buildRunnerService.rebuild(pendingAction.report)
            is LockBranchAction -> scmService.lockBranch(pendingAction.branch)
            else -> throw NotImplementedError("Unsupported pending action: $pendingAction")
        }
    }
}
