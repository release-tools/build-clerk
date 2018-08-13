package com.gatehill.buildclerk.service

import com.gatehill.buildclerk.api.model.Analysis
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.api.service.BuildReportService
import com.gatehill.buildclerk.config.Settings
import com.gatehill.buildclerk.dsl.AbstractBuildBlock
import com.gatehill.buildclerk.parser.Parser
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class AnalysisService @Inject constructor(
        private val parser: Parser,
        private val buildReportService: BuildReportService
) {
    private val logger = LogManager.getLogger(AnalysisService::class.java)

    fun analyseBuild(report: BuildReport): Analysis {
        val analysis = Analysis("Build ${report.build.number} on ${report.build.scm.branch}", logger)

        val config = parser.parse(Settings.Rules.configFile)

        val previousBuildStatus = buildReportService.fetchBuildStatus(
                branchName = report.build.scm.branch,
                buildNumber = report.build.number - 1
        )

        val blockConfigurer = { block: AbstractBuildBlock ->
            block.report = report
        }

        when (report.build.status) {
            BuildStatus.SUCCESS -> {
                logger.info("Build passed: $report")
                parser.invoke(
                        analysis = analysis,
                        branchName = report.build.scm.branch,
                        blockConfigurer = blockConfigurer,
                        body = config.bodyHolder.buildPassed
                )

                if (previousBuildStatus == BuildStatus.FAILED) {
                    logger.info("Branch started passing: $report")
                    parser.invoke(
                            analysis = analysis,
                            branchName = report.build.scm.branch,
                            blockConfigurer = blockConfigurer,
                            body = config.bodyHolder.branchStartsPassing
                    )
                }
            }
            BuildStatus.FAILED -> {
                logger.info("Build failed: $report")
                parser.invoke(
                        analysis = analysis,
                        branchName = report.build.scm.branch,
                        blockConfigurer = blockConfigurer,
                        body = config.bodyHolder.buildFailed
                )

                if (previousBuildStatus == BuildStatus.SUCCESS) {
                    logger.info("Branch started failing: $report")
                    parser.invoke(
                            analysis = analysis,
                            branchName = report.build.scm.branch,
                            blockConfigurer = blockConfigurer,
                            body = config.bodyHolder.branchStartsFailing
                    )
                }
            }
        }

        // runs every time
        parser.invoke(
                analysis = analysis,
                branchName = report.build.scm.branch,
                body = config.bodyHolder.repository
        )

        analysis.log("Analysis complete")
        return analysis
    }

    fun analysePullRequest(
            mergeEvent: PullRequestMergedEvent,
            currentBranchStatus: BuildStatus
    ): Analysis {

        val analysis = Analysis("Pull request ${mergeEvent.pullRequest.id} merged into ${mergeEvent.pullRequest.destination.branch.name}", logger)

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

        analysis.log("Analysis complete")
        return analysis
    }
}
