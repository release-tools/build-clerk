package com.gatehill.buildclerk.service

import com.gatehill.buildclerk.api.model.Analysis
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.api.service.BuildReportService
import com.gatehill.buildclerk.api.service.NotificationService
import com.gatehill.buildclerk.config.Settings
import com.gatehill.buildclerk.dsl.AbstractBuildBlock
import com.gatehill.buildclerk.parser.Parser
import com.gatehill.buildclerk.service.scm.PullRequestEventService
import com.gatehill.buildclerk.toShortCommit
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class AnalysisService @Inject constructor(
        private val parser: Parser,
        private val buildReportService: BuildReportService,
        private val pendingActionService: PendingActionService,
        private val notificationService: NotificationService,
        private val pullRequestEventService: PullRequestEventService
) {
    private val logger = LogManager.getLogger(AnalysisService::class.java)

    fun analyseBuild(report: BuildReport): Analysis {
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
        val branchName = report.build.scm.branch
        val commit = report.build.scm.commit

        val name = when (report.build.status) {
            BuildStatus.SUCCESS -> "${report.name} build #${report.build.number} passed on $branchName"
            BuildStatus.FAILED -> "${report.name} build #${report.build.number} failed on $branchName"
            else -> "${report.name} build #${report.build.number} on $branchName"
        }

        val analysis = Analysis(
                logger = logger,
                name = name,
                branch = branchName,
                user = report.build.triggeredBy,
                url = report.build.fullUrl
        )

        // perform some basic history checks on the commit
        val failuresForCommitOnBranch = buildReportService.countFailuresForCommitOnBranch(commit, branchName)
        val failureCountDescription = when (failuresForCommitOnBranch) {
            0 -> "never failed"
            1 -> "failed once"
            else -> "failed $failuresForCommitOnBranch times"
        }
        analysis.log("Commit `${toShortCommit(commit)}` has $failureCountDescription on this branch.")

        if (buildReportService.hasEverSucceeded(commit)) {
            analysis.log("This commit has previously succeeded (on at least 1 branch).")
        } else {
            analysis.log("This commit has never succeeded on any branch.")
        }

        // check if the commit originated from a PR
        pullRequestEventService.findPullRequestByMergeCommit(report.build.scm.commit)?.let { pullRequest ->
            analysis.log("This commit was introduced by PR: ${pullRequestEventService.describePullRequest(pullRequest)}")
        }

        return analysis
    }

    fun analysePullRequest(
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
