package io.gatehill.buildclerk.dao.mongo.model

import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.ZonedDateTime

class MongoPullRequestMergedEventWrapper(
    val mergeEvent: PullRequestMergedEvent,
    override val createdDate: ZonedDateTime
) : Dated {
    @BsonId
    val key: Id<MongoPullRequestMergedEventWrapper> = newId()
}

internal fun PullRequestMergedEvent.wrap() = MongoPullRequestMergedEventWrapper(
    mergeEvent = this,
    createdDate = ZonedDateTime.now()
)

