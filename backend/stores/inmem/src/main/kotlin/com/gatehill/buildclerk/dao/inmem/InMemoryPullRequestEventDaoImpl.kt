package com.gatehill.buildclerk.dao.inmem

import com.gatehill.buildclerk.api.dao.PullRequestEventDao
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent

class InMemoryPullRequestEventDaoImpl : PullRequestEventDao {
    private val store = mutableListOf<PullRequestMergedEvent>()

    override fun record(mergedEvent: PullRequestMergedEvent) {
        store += mergedEvent
    }

    override fun findByMergeCommit(commit: String): PullRequestMergedEvent?  =
        store.find { it.pullRequest.mergeCommit.hash == commit }
}
