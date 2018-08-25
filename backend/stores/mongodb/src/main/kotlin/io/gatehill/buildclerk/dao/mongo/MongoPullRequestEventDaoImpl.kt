package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.Commit
import io.gatehill.buildclerk.api.model.PullRequest
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.dao.mongo.model.MongoPullRequestMergedEvent
import io.gatehill.buildclerk.dao.mongo.model.toMongoPullRequestMergedEvent
import io.gatehill.buildclerk.dao.mongo.model.toPullRequestMergedEvent
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class MongoPullRequestEventDaoImpl : AbstractMongoDao(), PullRequestEventDao {
    override fun record(
        mergedEvent: PullRequestMergedEvent
    ) = withCollection<MongoPullRequestMergedEvent, Unit> {
        insertOne(mergedEvent.toMongoPullRequestMergedEvent())
    }

    override fun findByMergeCommit(
        commit: String
    ): PullRequestMergedEvent? =
        withCollection<MongoPullRequestMergedEvent, PullRequestMergedEvent?> {
            findOne(MongoPullRequestMergedEvent::pullRequest / PullRequest::mergeCommit / Commit::hash eq commit)
                ?.toPullRequestMergedEvent()
        }
}
