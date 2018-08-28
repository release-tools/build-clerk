package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.Commit
import io.gatehill.buildclerk.api.model.PullRequest
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.dao.mongo.model.MongoPullRequestMergedEventWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.time.ZonedDateTime

class MongoPullRequestEventDaoImpl : AbstractMongoDao(), PullRequestEventDao {
    override val collectionName = "pull_requests_merged"

    override fun record(
        mergedEvent: PullRequestMergedEvent
    ) = withCollection<MongoPullRequestMergedEventWrapper, Unit> {
        insertOne(mergedEvent.wrap())
    }

    override fun findByMergeCommit(
        commit: String
    ): PullRequestMergedEvent? =
        withCollection<MongoPullRequestMergedEventWrapper, PullRequestMergedEvent?> {
            findOne(MongoPullRequestMergedEventWrapper::mergeEvent / PullRequestMergedEvent::pullRequest / PullRequest::mergeCommit / Commit::hash eq commit)
                ?.mergeEvent
        }

    override val count
        get () = count<MongoPullRequestMergedEventWrapper>()

    override val oldestDate: ZonedDateTime?
        get() = oldestDate<MongoPullRequestMergedEventWrapper>()

    override val newestDate: ZonedDateTime?
        get() = newestDate<MongoPullRequestMergedEventWrapper>()
}
