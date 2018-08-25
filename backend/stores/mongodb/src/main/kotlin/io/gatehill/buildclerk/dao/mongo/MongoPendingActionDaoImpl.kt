package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.dao.mongo.model.MongoPendingActionWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class MongoPendingActionDaoImpl : AbstractMongoDao(), PendingActionDao {
    override fun save(actionSet: PendingActionSet) =
        withCollection<MongoPendingActionWrapper, Unit> {
            insertOne(actionSet.wrap())
        }

    override fun load(actionSetId: String): PendingActionSet? =
        withCollection<MongoPendingActionWrapper, PendingActionSet?> {
            findOne(MongoPendingActionWrapper::actionSet / PendingActionSet::id eq actionSetId)
                ?.actionSet
        }

    override fun delete(actionSetId: String) = withCollection<MongoPendingActionWrapper, Unit> {
        deleteOne(MongoPendingActionWrapper::actionSet / PendingActionSet::id eq actionSetId)
    }
}
