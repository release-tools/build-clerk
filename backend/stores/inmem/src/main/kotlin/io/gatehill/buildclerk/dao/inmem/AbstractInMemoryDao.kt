package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.dao.inmem.model.Record
import java.time.ZonedDateTime

abstract class AbstractInMemoryDao<T> : Recorded {
    internal abstract val store: MutableList<Record<T>>

    override val count
        get() = store.size

    override val oldestDate: ZonedDateTime?
        get() = store.firstOrNull()?.createdDate

    override val newestDate: ZonedDateTime?
        get() = store.lastOrNull()?.createdDate
}
