package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.api.config.Settings
import kotlinx.coroutines.experimental.launch
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Processes Bitbucket format pull requests.
 */
class BitbucketPullRequestEventServiceImpl @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService,
    private val pullRequestEventDao: PullRequestEventDao
) : PullRequestEventService {

    private val logger = LogManager.getLogger(PullRequestEventService::class.java)

    override val count
        get() = pullRequestEventDao.count

    override val oldestDate
        get() = pullRequestEventDao.oldestDate

    override val newestDate
        get() = pullRequestEventDao.newestDate

    override fun checkPullRequest(event: PullRequestMergedEvent) {
        logger.debug("Processing PR merge event: $event")

        pullRequestEventDao.record(event)

        val prInfo = "PR ${describePullRequest(event)}"
        val branchName = event.pullRequest.destination.branch.name

        if (Settings.EventFilter.repoNames.isNotEmpty() &&
            Settings.EventFilter.repoNames.none { it.matches(event.repository.name) }
        ) {
            logger.info("Ignoring merge event for $prInfo because repository name: ${event.repository.name} does not match filter")
            return
        }
        if (Settings.EventFilter.branchNames.isNotEmpty() &&
            Settings.EventFilter.branchNames.none { it.matches(branchName) }
        ) {
            logger.info("Ignoring merge event for $prInfo because branch name: $branchName does not match filter")
            return
        }

        launch {
            buildReportService.fetchLastBuildForBranch(branchName)?.let { buildReport ->
                val currentBranchStatus = buildReport.build.status
                logger.info("Status of branch: $branchName is $currentBranchStatus - triggering PR: $prInfo")
                analyse(event, currentBranchStatus)
            } ?: run {
                logger.warn("Status of branch: $branchName is unknown - triggering PR: $prInfo")
                analyse(event, BuildStatus.UNKNOWN)
            }
        }
    }

    override fun describePullRequest(event: PullRequestMergedEvent) =
        "#${event.pullRequest.id} '${event.pullRequest.title}' (author: ${event.pullRequest.author.username}, merged by: ${event.actor.username})"

    private fun analyse(
        event: PullRequestMergedEvent,
        currentBranchStatus: BuildStatus
    ) {
        analysisService.analysePullRequest(
            mergeEvent = event,
            currentBranchStatus = currentBranchStatus
        )
    }

    override fun findPullRequestByMergeCommit(commit: String): PullRequestMergedEvent? =
        pullRequestEventDao.findByMergeCommit(commit)
}
