package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.model.PullRequestMergedEvent

interface PullRequestEventDao {
    fun record(mergedEvent: PullRequestMergedEvent)
    fun findByMergeCommit(commit: String): PullRequestMergedEvent?
    fun count(): Int
}
