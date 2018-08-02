package com.gatehill.scmwebhook.service

import com.gatehill.scmwebhook.config.Settings
import com.gatehill.scmwebhook.model.BuildOutcome
import com.gatehill.scmwebhook.model.PullRequestMergedEvent
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager

class PullRequestEventService(
    private val branchStatusService: BranchStatusService
) {
    private val logger = LogManager.getLogger(PullRequestEventService::class.java)

    fun verify(event: PullRequestMergedEvent) {
        logger.debug("Processing PR merge event: $event")

        val prInfo = "merge event for PR #${event.pullRequest.id} [author: ${event.pullRequest.author.username}]"
        if (event.repository.name != Settings.repoName) {
            logger.info("Ignoring $prInfo because repository name: ${event.repository.name} does not match")
            return
        }
        val branchName = event.pullRequest.destination.branch.name
        if (branchName != Settings.branchName) {
            logger.info("Ignoring $prInfo because branch name: $branchName does not match")
            return
        }

        async {
            branchStatusService.fetchStatus(branchName)?.let { branchStatus ->
                val branchStatusInfo =
                    "branch name: $branchName status is currently: ${branchStatus.outcome}"

                when (branchStatus.outcome) {
                    BuildOutcome.SUCCESS -> logger.info("Verified $prInfo because $branchStatusInfo")
                    BuildOutcome.FAILED -> {
                        logger.warn("Failed to validate $prInfo because $branchStatusInfo")
                        // TODO take action, e.g. fire notification
                    }
                }

            } ?: run {
                logger.warn("Skipped validation of $prInfo because status of branch: $branchName is unknown")
            }
        }
    }
}
