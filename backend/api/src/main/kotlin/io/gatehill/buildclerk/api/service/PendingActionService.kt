package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.api.model.slack.ActionTriggeredEvent

interface PendingActionService : Recorded {
    fun enqueue(actionSet: PendingActionSet)
    fun handleAsync(event: ActionTriggeredEvent)
}
