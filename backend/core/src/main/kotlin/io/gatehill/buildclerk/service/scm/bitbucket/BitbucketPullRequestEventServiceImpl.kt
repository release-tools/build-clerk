package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.pr.PullRequestEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestEventType
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestModifiedEvent
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.api.util.shortenCommit
import kotlinx.coroutines.experimental.launch
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Processes Bitbucket format pull requests.
 */
class BitbucketPullRequestEventServiceImpl @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService,
    private val pullRequestEventDao: PullRequestEventDao,
    private val bitbucketOperationsService: BitbucketOperationsService
) : PullRequestEventService {

    private val logger = LogManager.getLogger(PullRequestEventService::class.java)

    override val count
        get() = pullRequestEventDao.count

    override val oldestDate
        get() = pullRequestEventDao.oldestDate

    override val newestDate
        get() = pullRequestEventDao.newestDate

    override fun checkPullRequest(event: PullRequestMergedEvent) {
        logger.debug("Processing PR merged event: $event")

        val normalised = normaliseCommitLengths(event)
        internalCheckPullRequest(normalised)
    }

    override fun checkModifiedPullRequest(event: PullRequestModifiedEvent, eventType: PullRequestEventType) {
        logger.debug("Processing PR $eventType event: $event")

        if (!shouldProcessPullRequest(event)) {
            return
        }

        analysisService.analyseModifiedPullRequest(event, eventType)
    }

    private fun internalCheckPullRequest(
        event: PullRequestMergedEvent
    ) {
        pullRequestEventDao.record(event)

        if (!shouldProcessPullRequest(event)) {
            return
        }

        val prInfo = "PR ${describePullRequest(event)}"
        val branchName = event.pullRequest.destination.branch.name

        launch {
            buildReportService.fetchLastReport(branchName)?.let { buildReport ->
                val currentBranchStatus = buildReport.build.status
                logger.info("Status of branch: $branchName is $currentBranchStatus - triggering PR: $prInfo")
                analyse(event, currentBranchStatus)
            } ?: run {
                logger.warn("Status of branch: $branchName is unknown - triggering PR: $prInfo")
                analyse(event, BuildStatus.UNKNOWN)
            }
        }
    }

    /**
     * @return `true` if the `PullRequestEvent` should be processed, given the configured filters
     */
    private fun shouldProcessPullRequest(event: PullRequestEvent): Boolean {
        val prInfo = "PR ${describePullRequest(event)}"
        val branchName = event.pullRequest.destination.branch.name

        if (Settings.EventFilter.repoNames.isNotEmpty() &&
            Settings.EventFilter.repoNames.none { it.matches(event.repository.name) }
        ) {
            logger.info("Ignoring event for $prInfo because repository name: ${event.repository.name} does not match filter")
            return false
        }
        if (Settings.EventFilter.branchNames.isNotEmpty() &&
            Settings.EventFilter.branchNames.none { it.matches(branchName) }
        ) {
            logger.info("Ignoring event for $prInfo because branch name: $branchName does not match filter")
            return false
        }

        return true
    }

    override fun describePullRequest(event: PullRequestEvent) =
        "#${event.pullRequest.id} '${event.pullRequest.title}' (author: ${event.pullRequest.author.username}, merged by: ${event.actor.username})"

    private fun analyse(
        event: PullRequestMergedEvent,
        currentBranchStatus: BuildStatus
    ) {
        analysisService.analyseMergedPullRequest(
            prEvent = event,
            currentBranchStatus = currentBranchStatus
        )
    }

    override fun ensureComment(pullRequestId: Int, comment: String) {
        val comments = bitbucketOperationsService.listComments(pullRequestId)

        val exists = comments.any { it.content.raw.trim() == comment.trim() }
        if (exists) {
            logger.info("Skipped adding existing comment '$comment' to PR $pullRequestId")
            return
        }

        logger.info("Adding comment '$comment' to PR $pullRequestId")
        bitbucketOperationsService.createComment(pullRequestId, comment)
    }

    /**
     * Normalise the length of `commit` using `toPrLengthCommit()` then query the DAO for a corresponding PR.
     */
    override fun findPullRequestByMergeCommit(commit: String): PullRequestMergedEvent? =
        pullRequestEventDao.findByMergeCommit(toPrLengthCommit(commit))

    override fun fetchLastPullRequest(branchName: String?): PullRequestMergedEvent? =
        pullRequestEventDao.fetchLast(branchName)

    override fun fetchPullRequests(branchName: String?): List<PullRequestMergedEvent> =
        pullRequestEventDao.list(branchName)

    /**
     * @return a copy of the `PullRequestMergedEvent` with source, destination and merge commit
     * hashes normalised using `toPrLengthCommit()`
     */
    private fun normaliseCommitLengths(event: PullRequestMergedEvent) = event.copy(
        pullRequest = event.pullRequest.copy(
            mergeCommit = event.pullRequest.mergeCommit.copy(
                hash = toPrLengthCommit(event.pullRequest.mergeCommit.hash)
            ),
            source = event.pullRequest.source.copy(
                commit = event.pullRequest.source.commit.copy(
                    hash = toPrLengthCommit(event.pullRequest.source.commit.hash)
                )
            ),
            destination = event.pullRequest.destination.copy(
                commit = event.pullRequest.destination.commit.copy(
                    hash = toPrLengthCommit(event.pullRequest.destination.commit.hash)
                )
            )
        )
    )

    /**
     * Normalise a commit hash to match BitBucket PR event length (12).
     */
    private fun toPrLengthCommit(commitHash: String) = shortenCommit(commitHash, 12)
}
