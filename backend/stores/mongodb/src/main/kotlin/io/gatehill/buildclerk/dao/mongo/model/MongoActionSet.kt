package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.action.PendingActionSet
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.ZonedDateTime

class MongoActionSetWrapper(
    val actionSet: PendingActionSet,
    override val createdDate: ZonedDateTime
) : Dated {
    @BsonId
    val key: Id<MongoActionSetWrapper> = newId()
}

internal fun PendingActionSet.wrap() = MongoActionSetWrapper(
    actionSet = this,
    createdDate = ZonedDateTime.now()
)
