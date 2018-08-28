package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.dao.mongo.model.MongoActionSetWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.time.ZonedDateTime

class MongoPendingActionDaoImpl : AbstractMongoDao(), PendingActionDao {
    override val collectionName = "pending_actions"

    override fun save(actionSet: PendingActionSet) =
        withCollection<MongoActionSetWrapper, Unit> {
            insertOne(actionSet.wrap())
        }

    override fun load(actionSetId: String): PendingActionSet? =
        withCollection<MongoActionSetWrapper, PendingActionSet?> {
            findOne(MongoActionSetWrapper::actionSet / PendingActionSet::id eq actionSetId)
                ?.actionSet
        }

    override fun delete(actionSetId: String) = withCollection<MongoActionSetWrapper, Unit> {
        deleteOne(MongoActionSetWrapper::actionSet / PendingActionSet::id eq actionSetId)
    }

    override val count
        get () = count<MongoActionSetWrapper>()

    override val oldestDate: ZonedDateTime?
        get() = oldestDate<MongoActionSetWrapper>()

    override val newestDate: ZonedDateTime?
        get() = newestDate<MongoActionSetWrapper>()
}
