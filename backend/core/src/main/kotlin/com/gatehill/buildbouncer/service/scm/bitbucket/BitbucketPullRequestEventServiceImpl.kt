package com.gatehill.buildbouncer.service.scm.bitbucket

import com.gatehill.buildbouncer.api.model.PullRequestMergedEvent
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.service.AnalysisService
import com.gatehill.buildbouncer.service.scm.PullRequestEventService
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Processes Bitbucket format pull requests.
 */
class BitbucketPullRequestEventServiceImpl @Inject constructor(
        private val buildOutcomeService: BuildOutcomeService,
        private val analysisService: AnalysisService
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
                logger.info("Performing validation checks on $prInfo with current branch name: $branchName status currently: ${buildOutcome.build.status}")

                analysisService.analysePullRequest(
                        mergeEvent = event,
                        currentBranchStatus = buildOutcome.build.status
                )

            } ?: run {
                logger.warn("Skipped validation checks for $prInfo because status of branch: $branchName is unknown")
            }
        }
    }
}
