package com.gatehill.buildclerk.dao.mongo

import com.gatehill.buildclerk.api.dao.PullRequestEventDao
import com.gatehill.buildclerk.api.model.Commit
import com.gatehill.buildclerk.api.model.PullRequest
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.dao.mongo.model.MongoPullRequestMergedEvent
import com.gatehill.buildclerk.dao.mongo.model.toMongoPullRequestMergedEvent
import com.gatehill.buildclerk.dao.mongo.model.toPullRequestMergedEvent
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
    ): PullRequestMergedEvent? = withCollection<MongoPullRequestMergedEvent, PullRequestMergedEvent?> {
        findOne(MongoPullRequestMergedEvent::pullRequest / PullRequest::mergeCommit / Commit::hash eq commit)
            ?.toPullRequestMergedEvent()
    }
}
