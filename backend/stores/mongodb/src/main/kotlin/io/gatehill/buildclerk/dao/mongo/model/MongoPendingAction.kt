package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.action.PendingActionSet
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

class MongoPendingActionWrapper(
    val actionSet: PendingActionSet
) {
    @BsonId
    val key: Id<MongoPendingActionWrapper> = newId()
}

internal fun PendingActionSet.wrap() = MongoPendingActionWrapper(
    actionSet = this
)
