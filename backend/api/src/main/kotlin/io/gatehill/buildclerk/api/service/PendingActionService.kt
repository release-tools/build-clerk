package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.api.model.slack.ActionTriggeredEvent

interface PendingActionService : Recorded {
    fun perform(actionSet: PendingActionSet)
    fun enqueue(actionSet: PendingActionSet)
    fun handleAsync(event: ActionTriggeredEvent)
}
