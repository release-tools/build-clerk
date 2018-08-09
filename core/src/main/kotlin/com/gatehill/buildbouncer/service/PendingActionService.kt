package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.api.model.action.LockBranchAction
import com.gatehill.buildbouncer.api.model.action.PendingAction
import com.gatehill.buildbouncer.api.model.action.PendingActionSet
import com.gatehill.buildbouncer.api.model.action.RebuildBranchAction
import com.gatehill.buildbouncer.api.model.action.RevertAction
import com.gatehill.buildbouncer.api.service.BuildRunnerService
import com.gatehill.buildbouncer.model.slack.ActionTriggeredEvent
import com.gatehill.buildbouncer.model.slack.SlackAttachmentAction
import com.gatehill.buildbouncer.model.slack.SlackMessage
import com.gatehill.buildbouncer.model.slack.SlackMessageAttachment
import com.gatehill.buildbouncer.service.scm.ScmService
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
        private val buildRunnerService: BuildRunnerService
) {
    private val logger: Logger = LogManager.getLogger(PendingActionService::class.java)
    private val pending = mutableMapOf<String, PendingActionSet>()

    fun enqueue(actionSet: PendingActionSet) {
        logger.info("Enqueuing ${actionSet.actions.size} pending actions: ${actionSet.actions}")
        pending[actionSet.id] = actionSet
    }

    fun handle(event: ActionTriggeredEvent): SlackMessage {
        logger.info("Handling action trigger with callback ID: ${event.callbackId}")

        // remove action buttons from message
        val attachments: MutableList<SlackMessageAttachment> = event.originalMessage.attachments
                ?.map { attachment -> attachment.copy(actions = emptyList()) }
                ?.toMutableList() ?: mutableListOf()

        event.actions?.let { actions ->
            val actionSetId = event.callbackId

            pending.remove(actionSetId)?.let { actionSet ->
                logger.debug("Found pending action set with ID: $actionSetId [${actionSet.actions.size} actions]")
                actions.forEach { action ->
                    val emoji = if(resolve(action, actionSet)) "white_check_mark" else "negative_squared_cross_mark"

                    // indicate outcome
                    attachments += SlackMessageAttachment(
                            text = ":$emoji: <@${event.user.id}> selected '${action.value}'"
                    )
                }

            } ?: logger.warn("No pending action set found with ID: $actionSetId")

        } ?: logger.warn("No actions found in event: $event")

        return event.originalMessage.copy(attachments = attachments)
    }

    private fun resolve(action: SlackAttachmentAction, actionSet: PendingActionSet) : Boolean {
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
            is RebuildBranchAction -> buildRunnerService.rebuild(pendingAction.outcome)
            is LockBranchAction -> scmService.lockBranch(pendingAction.branch)
            else -> throw NotImplementedError("Unsupported pending action: $pendingAction")
        }
    }
}
