package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.pr.Branch
import io.gatehill.buildclerk.api.model.pr.Commit
import io.gatehill.buildclerk.api.model.pr.MergedPullRequest
import io.gatehill.buildclerk.api.model.pr.PullRequest
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.RepoBranch
import io.gatehill.buildclerk.dao.mongo.model.Dated
import io.gatehill.buildclerk.dao.mongo.model.MongoPullRequestMergedEventWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
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
            findOne(MongoPullRequestMergedEventWrapper::mergeEvent / PullRequestMergedEvent::pullRequest / MergedPullRequest::mergeCommit / Commit::hash eq commit)
                ?.mergeEvent
        }

    override val count
        get () = count<MongoPullRequestMergedEventWrapper>()

    override val oldestDate: ZonedDateTime?
        get() = oldestDate<MongoPullRequestMergedEventWrapper>()

    override val newestDate: ZonedDateTime?
        get() = newestDate<MongoPullRequestMergedEventWrapper>()

    override fun fetchLast(
        branchName: String?
    ): PullRequestMergedEvent? = withCollection<MongoPullRequestMergedEventWrapper, PullRequestMergedEvent?> {
        val iterable = branchName?.let {
            find(MongoPullRequestMergedEventWrapper::mergeEvent / PullRequestMergedEvent::pullRequest / PullRequest::destination / RepoBranch::branch / Branch::name eq branchName)
        } ?: find()

        iterable
            .sort(descending(Dated::createdDate))
            .limit(1)
            .firstOrNull()
            ?.mergeEvent
    }

    override fun list(
        branchName: String?
    ): List<PullRequestMergedEvent> =
        withCollection<MongoPullRequestMergedEventWrapper, List<PullRequestMergedEvent>> {
            val iterable = branchName?.let {
                find(MongoPullRequestMergedEventWrapper::mergeEvent / PullRequestMergedEvent::pullRequest / PullRequest::destination / RepoBranch::branch / Branch::name eq branchName)
            } ?: find()

            // convert to list to avoid leaking mongo connection when method returns
            iterable
                .sort(ascending(Dated::createdDate))
                .map { it.mergeEvent }
                .toList()
        }
}
