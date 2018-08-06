package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.ActionTriggeredEvent
import com.gatehill.buildbouncer.api.model.PendingAction
import com.gatehill.buildbouncer.api.model.PendingActionSet
import com.gatehill.buildbouncer.api.model.RevertPendingAction
import com.gatehill.buildbouncer.model.SlackAction
import com.gatehill.buildbouncer.service.scm.ScmService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Completes pending actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class PendingActionService(
        private val scmService: ScmService
) {
    private val logger: Logger = LogManager.getLogger(PendingActionService::class.java)
    private val pending = mutableMapOf<String, PendingActionSet>()

    fun enqueue(actionSet: PendingActionSet) {
        logger.info("Enqueuing ${actionSet.actions.size} pending actions: ${actionSet.actions}")
        pending[actionSet.id] = actionSet
    }

    fun handle(event: ActionTriggeredEvent) {
        logger.info("Handling action trigger with callback ID: ${event.callbackId}")

        event.actions?.let { actions ->
            val actionSetId = event.callbackId

            pending.remove(actionSetId)?.let { actionSet ->
                logger.debug("Found pending action set with ID: $actionSetId [${actionSet.actions.size} actions]")
                actions.forEach { action -> resolve(action, actionSet) }

            } ?: logger.warn("No pending action set found with ID: $actionSetId")

        } ?: logger.warn("No actions found in event: $event")

        // TODO remove action buttons from message
    }

    private fun resolve(action: SlackAction, actionSet: PendingActionSet) {
        logger.debug("Attempting to resolve pending action: $action")

        actionSet.actions.find { it.name == action.name }?.let { pendingAction ->
            when (action.value) {
                pendingAction.name -> executePendingAction(pendingAction)
                else -> logger.info("Discarding pending action: $pendingAction [actionValue: ${action.value}]")
            }

        } ?: logger.warn("No such action '${action.name}' in pending action set: ${actionSet.id}")
    }

    private fun executePendingAction(pendingAction: PendingAction) {
        logger.info("Executing pending action: $pendingAction")
        when (pendingAction) {
            is RevertPendingAction -> scmService.revertCommit(pendingAction.commit, pendingAction.branch)
            else -> throw NotImplementedError("Unsupported pending action: $pendingAction")
        }
    }
}
