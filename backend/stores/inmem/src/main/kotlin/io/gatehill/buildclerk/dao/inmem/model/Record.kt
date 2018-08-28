package io.gatehill.buildclerk.dao.inmem.model

import java.time.ZonedDateTime

internal data class Record<T>(
    val createdDate: ZonedDateTime,
    val record: T
) {
    companion object {
        fun <T> create(record: T) = Record(
            createdDate = ZonedDateTime.now(),
            record = record
        )
    }
}
