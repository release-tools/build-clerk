package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.model.action.PendingActionSet

class InMemoryPendingActionDaoImpl : PendingActionDao {
    private val pending = mutableMapOf<String, PendingActionSet>()

    override fun save(actionSet: PendingActionSet) {
        pending[actionSet.id] = actionSet
    }

    override fun load(actionSetId: String): PendingActionSet? =
        pending[actionSetId]

    override fun delete(actionSetId: String) {
        pending.remove(actionSetId)
    }
}
