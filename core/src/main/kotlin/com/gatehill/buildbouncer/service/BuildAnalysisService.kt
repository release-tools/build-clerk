package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.parser.Parser
import org.apache.logging.log4j.LogManager

class BuildAnalysisService(
        private val parser: Parser
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    fun analyseBuild(outcome: BuildOutcome): Analysis {
        val analysis = Analysis("Build ${outcome.build.number} on ${outcome.build.scm.branch}", logger)

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> logger.info("No action required for successful build: $outcome")
            BuildStatus.FAILED -> analyseFailedBuild(outcome, analysis)
        }

        analysis.log("Analysis complete")
        return analysis
    }

    private fun analyseFailedBuild(outcome: BuildOutcome, analysis: Analysis) {
        parser.parse(Settings.Rules.configFile, outcome, analysis)
    }
}
