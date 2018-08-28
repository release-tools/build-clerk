package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.action.PendingActionSet

interface PendingActionDao : Recorded {
    fun save(actionSet: PendingActionSet)
    fun load(actionSetId: String): PendingActionSet?
    fun delete(actionSetId: String)
}
