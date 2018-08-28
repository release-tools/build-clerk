package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent

interface PullRequestEventDao: Recorded {
    fun record(mergedEvent: PullRequestMergedEvent)
    fun findByMergeCommit(commit: String): PullRequestMergedEvent?
}
