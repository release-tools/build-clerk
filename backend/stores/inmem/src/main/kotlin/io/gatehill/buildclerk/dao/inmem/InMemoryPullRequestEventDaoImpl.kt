package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.dao.inmem.model.Record

class InMemoryPullRequestEventDaoImpl : AbstractInMemoryDao<PullRequestMergedEvent>(), PullRequestEventDao {
    override val store = mutableListOf<Record<PullRequestMergedEvent>>()

    override fun record(mergedEvent: PullRequestMergedEvent) {
        store += Record.create(mergedEvent)
    }

    override fun findByMergeCommit(commit: String): PullRequestMergedEvent? =
        store.find { it.record.pullRequest.mergeCommit.hash == commit }?.record

    override fun fetchLast(branchName: String?): PullRequestMergedEvent? =
        store.asReversed().firstOrNull { wrapper ->
            branchName?.let { wrapper.record.pullRequest.destination.branch.name == branchName } ?: true
        }?.record

    override fun list(branchName: String?): List<PullRequestMergedEvent> =
        store.filter { wrapper ->
            branchName?.let { wrapper.record.pullRequest.destination.branch.name == branchName } ?: true
        }.map {
            it.record
        }
}
