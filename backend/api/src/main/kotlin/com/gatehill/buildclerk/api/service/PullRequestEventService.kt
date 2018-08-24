package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.PullRequestMergedEvent

/**
 * Processes pull request events.
 */
interface PullRequestEventService {
    fun checkPullRequest(event: PullRequestMergedEvent)
    fun describePullRequest(event: PullRequestMergedEvent): String
    fun findPullRequestByMergeCommit(commit: String): PullRequestMergedEvent?
}
