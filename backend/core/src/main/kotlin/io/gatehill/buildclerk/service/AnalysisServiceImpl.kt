package io.gatehill.buildclerk.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.api.util.toShortCommit
import io.gatehill.buildclerk.config.Settings
import io.gatehill.buildclerk.dsl.AbstractBuildBlock
import io.gatehill.buildclerk.parser.Parser
import io.gatehill.buildclerk.service.scm.ScmService
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.runBlocking
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

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

        val analysis = initBuildAnalysis(report)
        val branchName = report.build.scm.branch

        val previousBuildStatus = buildReportService.fetchBuildStatus(
            branchName = branchName,
            buildNumber = report.build.number - 1
        )

        val config = parser.parse(Settings.Rules.configFile)
        val blockConfigurer = { block: AbstractBuildBlock ->
            block.report = report
        }

        when (report.build.status) {
            BuildStatus.SUCCESS -> {
                parser.invoke(
                    analysis = analysis,
                    branchName = branchName,
                    blockConfigurer = blockConfigurer,
                    body = config.bodyHolder.buildPassed
                )

                if (previousBuildStatus == BuildStatus.FAILED) {
                    logger.info("Branch started passing: $report")
                    parser.invoke(
                        analysis = analysis,
                        branchName = branchName,
                        blockConfigurer = blockConfigurer,
                        body = config.bodyHolder.branchStartsPassing
                    )
                }
            }
            BuildStatus.FAILED -> {
                parser.invoke(
                    analysis = analysis,
                    branchName = branchName,
                    blockConfigurer = blockConfigurer,
                    body = config.bodyHolder.buildFailed
                )

                if (previousBuildStatus == BuildStatus.SUCCESS) {
                    logger.info("Branch started failing: $report")
                    parser.invoke(
                        analysis = analysis,
                        branchName = branchName,
                        blockConfigurer = blockConfigurer,
                        body = config.bodyHolder.branchStartsFailing
                    )
                }
            }
            else -> logger.warn("Unexpected build status: ${report.build.status} - skipped invoking build report events")
        }

        // runs every time
        parser.invoke(
            analysis = analysis,
            branchName = branchName,
            body = config.bodyHolder.repository
        )

        finaliseAnalysis(analysis)
        return analysis
    }

    private fun initBuildAnalysis(report: BuildReport): Analysis {
        val name = when (report.build.status) {
            BuildStatus.SUCCESS -> "${report.name} build #${report.build.number} passed on ${report.build.scm.branch}"
            BuildStatus.FAILED -> "${report.name} build #${report.build.number} failed on ${report.build.scm.branch}"
            else -> "${report.name} build #${report.build.number} on ${report.build.scm.branch}"
        }

        val analysis = Analysis(
            logger = logger,
            name = name,
            branch = report.build.scm.branch,
            user = report.build.triggeredBy,
            url = report.build.fullUrl
        )

        runBlocking {
            // perform some basic history checks on the commit
            val checkHistory = async {
                performHistoryChecks(report, analysis)
            }

            // check if the commit originated from a PR
            val findPr = async {
                pullRequestEventService.findPullRequestByMergeCommit(report.build.scm.commit)
            }

            // look up commit user info
            val fetchUserInfo = async {
                scmService.fetchUserInfoForCommit(report.build.scm.commit)
            }

            // wait for all jobs to complete, including ones with unit return value
            // to avoid race condition updates to analysis
            awaitAll(checkHistory, findPr, fetchUserInfo)

            findPr.getCompleted()?.let { pullRequest ->
                val prInfo = pullRequestEventService.describePullRequest(pullRequest)
                analysis.log("This commit was introduced by PR $prInfo")
            }

            fetchUserInfo.getCompleted().let { userInfo ->
                val sb = StringBuilder()
                sb.append("Author is ${userInfo.author.userName} <${userInfo.author.email}>")
                if (userInfo.author != userInfo.committer) {
                    sb.append(", committer is ${userInfo.author.userName} <${userInfo.author.email}>")
                }
                analysis.log(sb.toString())
            }
        }

        return analysis
    }

    /**
     * Perform some basic history checks on the commit.
     */
    private fun performHistoryChecks(
        report: BuildReport,
        analysis: Analysis
    ) {
        val branchName = report.build.scm.branch
        val commit = report.build.scm.commit

        val passedCount = analyseBranchStatus(commit, branchName, BuildStatus.SUCCESS, "passed")
        val failedCount = analyseBranchStatus(commit, branchName, BuildStatus.FAILED, "failed")
        analysis.log("Commit `${toShortCommit(commit)}` has $passedCount and $failedCount on this branch")

        if (report.build.status == BuildStatus.FAILED) {
            val passesOnBranch = buildReportService.countStatusForCommitOnBranch(
                commit = commit,
                branch = branchName,
                status = BuildStatus.FAILED
            )

            // check other branches for commit
            if (passesOnBranch == 0) {
                if (buildReportService.hasEverSucceeded(commit)) {
                    analysis.log("This commit has previously succeeded (on at least 1 branch)")
                } else {
                    analysis.log("This commit has never succeeded on any branch")
                }
            }
        }
    }

    /**
     * @param statusDescription past tense outcome, e.g. 'passed' or 'failed'
     */
    private fun analyseBranchStatus(
        commit: String,
        branchName: String,
        status: BuildStatus,
        statusDescription: String
    ) : String {
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

    override fun analysePullRequest(
        mergeEvent: PullRequestMergedEvent,
        currentBranchStatus: BuildStatus
    ): Analysis {

        val analysis = Analysis(
            logger = logger,
            name = "Pull request #${mergeEvent.pullRequest.id} merged into ${mergeEvent.pullRequest.destination.branch.name}",
            branch = mergeEvent.pullRequest.destination.branch.name,
            user = mergeEvent.actor.displayName
        )

        val config = parser.parse(Settings.Rules.configFile)

        parser.invoke(
            analysis = analysis,
            branchName = mergeEvent.pullRequest.destination.branch.name,
            body = config.bodyHolder.pullRequestMerged,
            blockConfigurer = { block ->
                block.mergeEvent = mergeEvent
                block.currentBranchStatus = currentBranchStatus
            }
        )

        finaliseAnalysis(analysis)
        return analysis
    }

    /**
     * Enqueue actions and send notifications.
     */
    private fun finaliseAnalysis(analysis: Analysis) {
        if (analysis.isNotEmpty()) {
            pendingActionService.enqueue(analysis.actionSet)
        }
        analysis.postConfig?.let { postConfig ->
            notificationService.notify(postConfig.channelName, analysis, postConfig.color.hexCode)
        }
    }
}
