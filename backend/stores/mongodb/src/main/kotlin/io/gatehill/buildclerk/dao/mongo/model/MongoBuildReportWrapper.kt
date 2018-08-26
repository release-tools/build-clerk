package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.BuildReport
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.ZonedDateTime

data class MongoBuildReportWrapper(
    val buildReport: BuildReport,
    val createdDate: ZonedDateTime
) {
    @BsonId
    val key: Id<MongoBuildReportWrapper> = newId()
}

internal fun BuildReport.wrap() = MongoBuildReportWrapper(
    buildReport = this,
    createdDate = ZonedDateTime.now()
)
