package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.BranchNotification
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.ZonedDateTime

class MongoBranchNotificationWrapper(
    val notification: BranchNotification,
    override val createdDate: ZonedDateTime
) : Dated {
    @BsonId
    val key: Id<MongoBranchNotificationWrapper> = newId()
}

internal fun BranchNotification.wrap() = MongoBranchNotificationWrapper(
    notification = this,
    createdDate = ZonedDateTime.now()
)
