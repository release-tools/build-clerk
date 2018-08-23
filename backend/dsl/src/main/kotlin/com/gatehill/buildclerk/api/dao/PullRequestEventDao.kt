package com.gatehill.buildclerk.api.dao

import com.gatehill.buildclerk.api.model.PullRequestMergedEvent

interface PullRequestEventDao {
    fun record(mergedEvent: PullRequestMergedEvent)
    fun findByMergeCommit(commit: String): PullRequestMergedEvent?
}
