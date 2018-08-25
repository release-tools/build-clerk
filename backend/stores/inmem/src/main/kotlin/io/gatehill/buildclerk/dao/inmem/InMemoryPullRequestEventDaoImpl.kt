package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent

class InMemoryPullRequestEventDaoImpl : PullRequestEventDao {
    private val store = mutableListOf<PullRequestMergedEvent>()

    override fun record(mergedEvent: PullRequestMergedEvent) {
        store += mergedEvent
    }

    override fun findByMergeCommit(commit: String): PullRequestMergedEvent? =
        store.find { it.pullRequest.mergeCommit.hash == commit }
}
