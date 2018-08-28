package io.gatehill.buildclerk.dao.mongo.model

import java.time.ZonedDateTime

interface Dated {
    val createdDate: ZonedDateTime
}
