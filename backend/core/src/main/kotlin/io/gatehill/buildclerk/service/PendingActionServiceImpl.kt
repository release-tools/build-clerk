package io.gatehill.buildclerk.service

import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.model.action.LockBranchAction
import io.gatehill.buildclerk.api.model.action.PendingAction
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.api.model.action.RebuildBranchAction
import io.gatehill.buildclerk.api.model.action.RevertCommitAction
import io.gatehill.buildclerk.api.model.action.ShowTextAction
import io.gatehill.buildclerk.api.model.message.MessageAction
import io.gatehill.buildclerk.api.model.message.MessageAttachment
import io.gatehill.buildclerk.api.model.message.UpdatedNotificationMessage
import io.gatehill.buildclerk.api.model.slack.ActionTriggeredEvent
import io.gatehill.buildclerk.api.model.slack.SlackMessageAction
import io.gatehill.buildclerk.api.model.slack.SlackMessageAttachment
import io.gatehill.buildclerk.api.service.BuildRunnerService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.service.PendingActionService
import io.gatehill.buildclerk.service.notify.slack.toMessageAction
import io.gatehill.buildclerk.service.notify.slack.toMessageAttachment
import io.gatehill.buildclerk.service.scm.ScmService
import kotlinx.coroutines.experimental.launch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Completes pending actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class PendingActionServiceImpl @Inject constructor(
    private val scmService: ScmService,
    private val buildRunnerService: BuildRunnerService,
    private val notificationService: NotificationService,
    private val pendingActionDao: PendingActionDao
) : PendingActionService {

    private val logger: Logger = LogManager.getLogger(PendingActionService::class.java)

    override val count
        get() = pendingActionDao.count

    override val oldestDate
        get() = pendingActionDao.oldestDate

    override val newestDate
        get() = pendingActionDao.newestDate

    override fun perform(actionSet: PendingActionSet) {
        when (actionSet.actions.size) {
            0 -> logger.debug("No actions to perform")
            else -> {
                logger.info("Performing ${actionSet.actions.size} actions: ${actionSet.actions}")
                actionSet.actions.forEach { action -> perform(null, action) }
            }
        }
    }

    override fun enqueue(actionSet: PendingActionSet) {
        when (actionSet.actions.size) {
            0 -> logger.debug("No actions to enqueue")
            else -> {
                logger.info("Enqueuing ${actionSet.actions.size} actions: ${actionSet.actions}")
                pendingActionDao.save(actionSet)
            }
        }
    }

    override fun handleAsync(event: ActionTriggeredEvent) {
        launch {
            try {
                handle(event)
            } catch (e: Exception) {
                logger.error("Error handling action trigger with callback ID: ${event.callbackId}", e)
            }
        }
    }

    private fun handle(event: ActionTriggeredEvent) {
        logger.info("Handling action trigger with callback ID: ${event.callbackId}")

        event.actions?.let { actions ->
            val actionSetId = event.callbackId

            pendingActionDao.load(actionSetId)?.let { actionSet ->
                logger.debug("Found pending action set with ID: $actionSetId [${actionSet.actions.size} actions]")
                resolveActions(event, actions, actionSet)

            } ?: logger.warn("No pending action set found with ID: $actionSetId")

        } ?: logger.warn("No actions found in event: $event")
    }

    private fun resolveActions(
        event: ActionTriggeredEvent,
        actions: List<SlackMessageAction>,
        pendingActionSet: PendingActionSet
    ) {
        val selectedActions = mutableListOf<SelectedAction>()

        actions.forEach { action ->
            pendingActionSet.actions.find { it.name == action.name }?.let { pendingAction ->
                val executed = resolve(event.channel.name, action, pendingAction)

                selectedActions += SelectedAction(
                    actionName = pendingAction.name,
                    exclusive = pendingAction.exclusive,
                    outcomeText = if (executed) {
                        ":white_check_mark: <@${event.user.id}> selected '${pendingAction.title}'"
                    } else {
                        ":-1: <@${event.user.id}> dismissed suggested action: '${pendingAction.title}'"
                    }
                )

                if (executed && pendingAction.exclusive) {
                    logger.debug("Selected action: ${pendingAction.name} is exclusive - removing action set with ID: ${pendingActionSet.id}")
                    pendingActionDao.delete(pendingActionSet.id)
                }

            } ?: logger.warn("No such action '${action.name}' in pending action set: ${pendingActionSet.id}")
        }

        val attachments = event.originalMessage.attachments?.let { slackAttachments ->
            composeAttachments(
                slackAttachments = slackAttachments,
                selectedActions = selectedActions
            )
        } ?: emptyList()

        // update the original message
        updateOriginalMessage(event, attachments)
    }

    internal fun composeAttachments(
        slackAttachments: List<SlackMessageAttachment>,
        selectedActions: List<SelectedAction>
    ): List<MessageAttachment> {

        val attachments = mutableListOf<MessageAttachment>()

        attachments += slackAttachments.mapNotNull { slackAttachment ->
            val slackActions = slackAttachment.actions

            if (slackActions?.isEmpty() != false) {
                // attachment has no actions - include it
                slackAttachment.toMessageAttachment(emptyList())

            } else if (selectedActions.any { it.exclusive }) {
                // if exclusive action was executed skip all attachments with actions
                null

            } else {
                if (isAttachmentActionSelected(slackActions, selectedActions)) {
                    // if attachment's action was selected skip it (outcome rendered later)
                    null

                } else {
                    // for attachments with actions, include only unselected actions
                    val actions = filterUnselectedActions(slackActions, selectedActions)
                    slackAttachment.toMessageAttachment(actions)
                }
            }
        }

        // add outcomes for selected actions
        attachments += selectedActions.map { selectedAction ->
            MessageAttachment(
                text = selectedAction.outcomeText
            )
        }

        return attachments
    }

    private fun isAttachmentActionSelected(
        slackActions: List<SlackMessageAction>,
        selectedActions: List<SelectedAction>
    ): Boolean {
        return slackActions.any { slackAction ->
            selectedActions.any { selectedAction ->
                slackAction.name == selectedAction.actionName
            }
        }
    }

    /**
     * @return only actions have have not been selected
     */
    private fun filterUnselectedActions(
        slackActions: List<SlackMessageAction>?,
        selectedActions: List<SelectedAction>
    ): List<MessageAction> {

        return slackActions?.mapNotNull { slackAction ->
            if (selectedActions.any { it.actionName == slackAction.name }) {
                // skip selected actions
                null
            } else {
                // include unresolved actions
                slackAction.toMessageAction()
            }
        } ?: emptyList()
    }

    private fun updateOriginalMessage(
        event: ActionTriggeredEvent,
        attachments: List<MessageAttachment>
    ) {
        event.originalMessage.ts?.let { ts ->
            val updatedMessage = UpdatedNotificationMessage(
                messageId = ts,
                channel = event.channel.id,
                text = event.originalMessage.text,
                attachments = attachments
            )
            notificationService.updateMessage(updatedMessage)

        } ?: logger.warn(
            "Cannot update original message will callback ID: ${event.callbackId}, as there was no message timestamp"
        )
    }

    private fun resolve(
        triggeringChannel: String,
        action: SlackMessageAction,
        pendingAction: PendingAction
    ): Boolean {
        logger.debug("Attempting to resolve pending action: $action")

        when (action.value) {
            pendingAction.name -> {
                perform(triggeringChannel, pendingAction)
                return true
            }
            else -> logger.info("Discarding pending action: $pendingAction [actionValue: ${action.value}]")
        }

        return false
    }

    private fun perform(
        triggeringChannel: String?,
        pendingAction: PendingAction
    ) {
        logger.info("Executing pending action: $pendingAction")
        when (pendingAction) {
            is RevertCommitAction -> scmService.revertCommit(
                pendingAction.commit,
                pendingAction.branch
            )
            is RebuildBranchAction -> buildRunnerService.rebuild(pendingAction.report)
            is LockBranchAction -> scmService.lockBranch(pendingAction.branch)
            is ShowTextAction -> showText(triggeringChannel, pendingAction)
            else -> throw NotImplementedError("Unsupported pending action: $pendingAction")
        }
    }

    /**
     * Show text on the specified channel, or the triggering channel if it is not specified.
     */
    private fun showText(triggeringChannel: String?, action: ShowTextAction) {
        val channelName = action.channelName ?: triggeringChannel
        channelName?.let {
            notificationService.notify(channelName, action.body, action.color.hexCode)
        } ?: run {
            logger.error("No channel name set or triggering channel provided for action: $action")
        }
    }
}

internal data class SelectedAction(
    val actionName: String,
    val exclusive: Boolean,
    val outcomeText: String
)
