package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.action.PendingAction
import com.gatehill.buildbouncer.model.action.RevertPendingAction
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
    private val pending = mutableMapOf<String, PendingAction>()

    fun enqueue(pendingActions: List<PendingAction>) {
        pendingActions.forEach(this::enqueue)
    }

    fun enqueue(pendingAction: PendingAction) {
        logger.info("Enqueuing pending action: $pendingAction")
        pending[pendingAction.id] = pendingAction
    }

    fun resolve(pendingActionId: String, actionValue: String) {
        logger.debug("Attempting to resolve pending action with ID: $pendingActionId")

        pending.remove(pendingActionId)?.let { pendingAction ->
            when (actionValue) {
                "approve" -> executePendingAction(pendingAction)
                else -> logger.info("Discarding pending action: $pendingAction [actionValue: $actionValue]")
            }

        } ?: logger.warn("No pending action found with ID: $pendingActionId")
    }

    private fun executePendingAction(pendingAction: PendingAction) {
        logger.info("Executing pending action: $pendingAction")
        when (pendingAction) {
            is RevertPendingAction -> scmService.revertCommit(pendingAction.commit, pendingAction.branch)
            else -> throw NotImplementedError("Unsupported pending action: $pendingAction")
        }
    }
}
