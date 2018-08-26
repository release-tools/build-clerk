package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.model.action.PendingActionSet

interface PendingActionDao {
    fun save(actionSet: PendingActionSet)
    fun load(actionSetId: String): PendingActionSet?
    fun delete(actionSetId: String)
    fun count(): Int
}
