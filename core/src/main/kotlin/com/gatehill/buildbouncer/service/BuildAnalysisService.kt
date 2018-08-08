package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.dsl.ConfigWrapper
import com.gatehill.buildbouncer.parser.Parser
import org.apache.logging.log4j.LogManager

class BuildAnalysisService(
        private val parser: Parser,
        private val buildOutcomeService: BuildOutcomeService
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    fun analyseBuild(outcome: BuildOutcome): Analysis {
        val analysis = Analysis("Build ${outcome.build.number} on ${outcome.build.scm.branch}", logger)

        val rulesResult = parser.parse(Settings.Rules.configFile, outcome, analysis)

        val previousBuild = buildOutcomeService.fetchBuildStatus(
            branchName = outcome.build.scm.branch,
            buildNumber = outcome.build.number - 1
        )

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> {
                logger.info("Successful build: $outcome")
                rulesResult.invokeBuildPassed()
                if (previousBuild == BuildStatus.FAILED) {
                    rulesResult.invokeBranchStartsPassing()
                }
            }
            BuildStatus.FAILED -> {
                logger.info("Failed build: $outcome")
                rulesResult.invokeBuildFailed()
                if (previousBuild == BuildStatus.SUCCESS) {
                    rulesResult.invokeBranchStartsFailing()
                }
            }
        }

        // runs every time
        rulesResult.invokeRepository()

        analysis.log("Analysis complete")
        return analysis
    }
}
