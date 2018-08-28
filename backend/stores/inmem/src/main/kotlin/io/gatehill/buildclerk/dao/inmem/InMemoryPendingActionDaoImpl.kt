package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.dao.inmem.model.Record
import java.time.ZonedDateTime

class InMemoryPendingActionDaoImpl : PendingActionDao {
    private val store = linkedMapOf<String, Record<PendingActionSet>>()

    override fun save(actionSet: PendingActionSet) {
        store[actionSet.id] = Record.create(actionSet)
    }

    override fun load(actionSetId: String): PendingActionSet? =
        store[actionSetId]?.record

    override fun delete(actionSetId: String) {
        store.remove(actionSetId)
    }

    override val count
        get() = store.size

    override val oldestDate: ZonedDateTime?
        get() = store.values.firstOrNull()?.createdDate

    override val newestDate: ZonedDateTime?
        get() = store.values.lastOrNull()?.createdDate
}
