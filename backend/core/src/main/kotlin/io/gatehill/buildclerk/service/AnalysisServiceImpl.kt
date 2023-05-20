package io.gatehill.buildclerk.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.ReportSpan
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.model.pr.PullRequestEventType
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestModifiedEvent
import io.gatehill.buildclerk.api.model.pr.SourceFile
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.service.PendingActionService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.api.util.toShortCommit
import io.gatehill.buildclerk.dsl.BuildBlock
import io.gatehill.buildclerk.parser.Parser
import io.gatehill.buildclerk.service.scm.ScmService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import java.time.ZonedDateTime
import java.util.Collections.synchronizedList
import javax.inject.Inject

/**
 * Performs analyses of certain events.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class AnalysisServiceImpl @Inject constructor(
    private val parser: Parser,
    private val buildReportService: BuildReportService,
    private val pendingActionService: PendingActionService,
    private val notificationService: NotificationService,
    private val pullRequestEventService: PullRequestEventService,
    private val scmService: ScmService
) : AnalysisService {

    private val logger = LogManager.getLogger(AnalysisService::class.java)

    override fun analyseBuild(report: BuildReport): Analysis {
        logger.info("Analysing build report: $report")

        val branchName = report.build.scm.branch
        val analysisName = buildShortDescription(report)

        val analysis = Analysis(
            logger = logger,
            name = analysisName,
            branch = branchName,
            user = report.build.triggeredBy,
            url = report.build.fullUrl
        )

        performBasicBuildAnalysis(report).forEach { analysis.log(it) }

        val previousBuildStatus = buildReportService.fetchBuildStatus(
            branchName = branchName,
            buildNumber = report.build.number - 1
        )

        val config = parser.parse()
        val blockConfigurer = { block: BuildBlock ->
            block.analysis = analysis
            block.report = report
        }

        when (report.build.status) {
            BuildStatus.SUCCESS -> {
                parser.invoke(
                    blockConfigurer = blockConfigurer,
                    body = config.bodyHolder.buildPassed
                )

                if (previousBuildStatus == BuildStatus.FAILED) {
                    buildReportService.findHigherBuild(branchName, report.build.number)?.let {
                        logger.debug("Skipped firing event for branch '$branchName' state transition to passing as newer build already recorded")

                    } ?: run {
                        logger.info("Branch '$branchName' state transitioned to passing")
                        parser.invoke(
                            blockConfigurer = blockConfigurer,
                            body = config.bodyHolder.branchStartsPassing
                        )
                    }
                }
            }
            BuildStatus.FAILED -> {
                parser.invoke(
                    blockConfigurer = blockConfigurer,
                    body = config.bodyHolder.buildFailed
                )

                if (previousBuildStatus == BuildStatus.SUCCESS) {
                    buildReportService.findHigherBuild(branchName, report.build.number)?.let {
                        logger.debug("Skipped firing event for branch '$branchName' state transition to failing as newer build already recorded")

                    } ?: run {
                        logger.info("Branch '$branchName' state transitioned to failing")
                        parser.invoke(
                            blockConfigurer = blockConfigurer,
                            body = config.bodyHolder.branchStartsFailing
                        )
                    }
                }
            }
            else -> logger.warn("Unexpected build status: ${report.build.status} - skipped invoking build report events")
        }

        // runs every time
        parser.invoke(
            blockConfigurer = blockConfigurer,
            body = config.bodyHolder.repository
        )

        finaliseAnalysis(analysis)
        return analysis
    }

    override fun buildShortDescription(report: BuildReport): String {
        return when (report.build.status) {
            BuildStatus.SUCCESS -> "${report.name} build #${report.build.number} passed on ${report.build.scm.branch}"
            BuildStatus.FAILED -> "${report.name} build #${report.build.number} failed on ${report.build.scm.branch}"
            else -> "${report.name} build #${report.build.number} on ${report.build.scm.branch}"
        }
    }

    override fun performBasicBuildAnalysis(report: BuildReport): List<String> {
        val analysisLines = synchronizedList(mutableListOf<String>())

        runBlocking {
            // perform some basic history checks on the commit
            val checkHistory = async {
                performHistoryChecks(report)
            }

            // check if the commit originated from a PR
            val findPr = async {
                pullRequestEventService.findPullRequestByMergeCommit(report.build.scm.commit)
            }

            // look up commit user info
            val fetchUserInfo = async {
                scmService.fetchUserInfoForCommit(report.build.scm.commit)
            }

            analysisLines += checkHistory.await()

            findPr.await()?.let { pullRequest ->
                val prInfo = pullRequestEventService.describePullRequest(pullRequest)
                analysisLines += "This commit was introduced by PR $prInfo"
            }

            fetchUserInfo.await().let { userInfo ->
                val sb = StringBuilder()
                sb.append("Author is ${userInfo.author.userName} <${userInfo.author.email}>")
                if (userInfo.author != userInfo.committer) {
                    sb.append(", committer is ${userInfo.author.userName} <${userInfo.author.email}>")
                }
                analysisLines += sb.toString()
            }
        }

        return analysisLines
    }

    /**
     * Perform some basic history checks on the commit.
     */
    private fun performHistoryChecks(report: BuildReport): List<String> {
        val analysisLines = mutableListOf<String>()
        val branchName = report.build.scm.branch
        val commit = report.build.scm.commit

        val commitHistory = analyseCommitHistory(branchName, commit)
        analysisLines += "Commit `${toShortCommit(commit)}` has $commitHistory"

        if (report.build.status == BuildStatus.FAILED) {
            val passesOnBranch = buildReportService.countStatusForCommitOnBranch(
                commit = commit,
                branch = branchName,
                status = BuildStatus.FAILED
            )

            // check other branches for commit
            if (passesOnBranch == 0) {
                if (buildReportService.hasEverSucceeded(commit)) {
                    analysisLines += "This commit has previously succeeded (on at least 1 branch)"
                } else {
                    analysisLines += "This commit has never succeeded on any branch"
                }
            }
        }

        return analysisLines
    }

    private fun analyseCommitHistory(branchName: String, commit: String): String {
        val passedCount = analyseBranchStatus(commit, branchName, BuildStatus.SUCCESS, "passed")
        val failedCount = analyseBranchStatus(commit, branchName, BuildStatus.FAILED, "failed")
        return "$passedCount and $failedCount on this branch"
    }

    /**
     * @param statusDescription past tense outcome, e.g. 'passed' or 'failed'
     */
    private fun analyseBranchStatus(
        commit: String,
        branchName: String,
        status: BuildStatus,
        statusDescription: String
    ): String {
        val statusCount = buildReportService.countStatusForCommitOnBranch(
            commit = commit,
            branch = branchName,
            status = status
        )
        return when (statusCount) {
            0 -> "never $statusDescription"
            1 -> "$statusDescription once"
            2 -> "$statusDescription twice"
            else -> "$statusDescription $statusCount times"
        }
    }

    override fun analyseModifiedPullRequest(
        prEvent: PullRequestModifiedEvent,
        eventType: PullRequestEventType
    ): Analysis {

        val analysis = Analysis(
            logger = logger,
            name = "Pull request #${prEvent.pullRequest.id} $eventType, with destination branch ${prEvent.pullRequest.destination.branch.name}",
            branch = prEvent.pullRequest.destination.branch.name,
            user = prEvent.actor.displayName
        )

        // defer calculation of file diff until required
        val filesChanged: List<SourceFile> by lazy {
            scmService.listModifiedFiles(prEvent.pullRequest.id)
        }

        val config = parser.parse()

        parser.invoke(
            body = config.bodyHolder.pullRequestModified,
            blockConfigurer = { block ->
                block.analysis = analysis
                block.pullRequestEvent = prEvent
                block.files = filesChanged
            }
        )

        finaliseAnalysis(analysis)
        return analysis
    }

    override fun analyseMergedPullRequest(
        prEvent: PullRequestMergedEvent,
        currentBranchStatus: BuildStatus
    ): Analysis {

        val analysis = Analysis(
            logger = logger,
            name = "Pull request #${prEvent.pullRequest.id} merged into ${prEvent.pullRequest.destination.branch.name}",
            branch = prEvent.pullRequest.destination.branch.name,
            user = prEvent.actor.displayName
        )

        val config = parser.parse()

        parser.invoke(
            body = config.bodyHolder.pullRequestMerged,
            blockConfigurer = { block ->
                block.analysis = analysis
                block.pullRequestEvent = prEvent
                block.currentBranchStatus = currentBranchStatus
            }
        )

        finaliseAnalysis(analysis)
        return analysis
    }

    /**
     * Perform and enqueue actions, then send analysis notification.
     */
    private fun finaliseAnalysis(analysis: Analysis) {
        if (analysis.isNotEmpty()) {
            pendingActionService.perform(analysis.perform)
            pendingActionService.enqueue(analysis.suggested)
        }

        analysis.publishConfig?.let { postConfig ->
            notificationService.notify(postConfig.channelName, analysis, postConfig.color.hexCode)
        }
    }

    override fun analyseReportSpan(
        branchName: String?,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): ReportSpan = buildReportService.fetchReportsBetween(branchName, start, end).let { reports ->

        val reportCount = reports.size
        val successful = reports.count { it.build.status == BuildStatus.SUCCESS }
        val failed = reports.count { it.build.status == BuildStatus.FAILED }

        ReportSpan(
            dataPoints = reportCount,
            successful = successful,
            failed = failed,
            passRate = when (reportCount) {
                0 -> 0.toDouble()
                else -> successful / reportCount.toDouble()
            }
        )
    }
}
