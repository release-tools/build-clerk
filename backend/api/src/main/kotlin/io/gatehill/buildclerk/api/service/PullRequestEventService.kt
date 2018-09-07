package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.pr.PullRequestModifiedEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestEventType
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent

/**
 * Processes pull request events.
 */
interface PullRequestEventService : Recorded {
    fun checkPullRequest(event: PullRequestMergedEvent)
    fun checkModifiedPullRequest(event: PullRequestModifiedEvent, eventType: PullRequestEventType)
    fun describePullRequest(event: PullRequestEvent): String
    fun findPullRequestByMergeCommit(commit: String): PullRequestMergedEvent?
    fun fetchLastPullRequest(branchName: String? = null): PullRequestMergedEvent?
    fun fetchPullRequests(branchName: String? = null): List<PullRequestMergedEvent>

    /**
     * Ensure the given comment exists on the specified PR, otherwise create it.
     */
    fun ensureComment(pullRequestId: Int, comment: String)
}
