package com.gatehill.buildbouncer.service.scm

import com.gatehill.buildbouncer.model.bitbucket.PullRequestMergedEvent

/**
 * Processes pull request events.
 */
interface PullRequestEventService {
    fun checkPullRequest(event: PullRequestMergedEvent)
}
