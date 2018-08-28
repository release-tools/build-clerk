package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.dao.inmem.model.Record

class InMemoryPullRequestEventDaoImpl : AbstractInMemoryDao<PullRequestMergedEvent>(), PullRequestEventDao {
    override val store = mutableListOf<Record<PullRequestMergedEvent>>()

    override fun record(mergedEvent: PullRequestMergedEvent) {
        store += Record.create(mergedEvent)
    }

    override fun findByMergeCommit(commit: String): PullRequestMergedEvent? =
        store.find { it.record.pullRequest.mergeCommit.hash == commit }?.record
}
