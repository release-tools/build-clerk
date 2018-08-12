package com.gatehill.buildbouncer.service.scm.bitbucket

import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.model.bitbucket.PullRequestMergedEvent
import com.gatehill.buildbouncer.service.scm.PullRequestEventService
import com.gatehill.buildbouncer.service.scm.ScmService
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Processes Bitbucket format pull requests.
 */
class BitbucketPullRequestEventServiceImpl @Inject constructor(
        private val buildOutcomeService: BuildOutcomeService,
        private val scmService: ScmService
) : PullRequestEventService {

    private val logger = LogManager.getLogger(PullRequestEventService::class.java)

    override fun checkPullRequest(event: PullRequestMergedEvent) {
        logger.debug("Processing PR merge event: $event")

        val prInfo = "merge event for PR #${event.pullRequest.id} [author: ${event.pullRequest.author.username}]"
        val branchName = event.pullRequest.destination.branch.name

        Settings.EventFilter.repoName?.takeIf(String::isNotBlank)?.let { filterRepoName ->
            if (event.repository.name != filterRepoName) {
                logger.info("Ignoring $prInfo because repository name: ${event.repository.name} does not match filter")
                return
            }
        }
        Settings.EventFilter.branchName?.takeIf(String::isNotBlank)?.let { filterBranchName ->
            if (branchName != filterBranchName) {
                logger.info("Ignoring $prInfo because branch name: $branchName does not match filter")
                return
            }
        }

        @Suppress("DeferredResultUnused")
        async {
            buildOutcomeService.fetchStatus(branchName)?.let { buildOutcome ->
                val branchStatusInfo =
                        "branch name: $branchName status is currently: ${buildOutcome.build.status}"

                when (buildOutcome.build.status) {
                    BuildStatus.SUCCESS -> logger.info("Verification checks passed for $prInfo because $branchStatusInfo")
                    BuildStatus.FAILED -> {
                        logger.warn("Verification checks failed for $prInfo because $branchStatusInfo - reverting merge")
                        scmService.revertCommit(
                                commit = event.pullRequest.mergeCommit.hash,
                                branchName = branchName
                        )
                    }
                }

            } ?: run {
                logger.warn("Skipped validation of $prInfo because status of branch: $branchName is unknown")
            }
        }
    }
}
