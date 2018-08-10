package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.dsl.AbstractBlock
import com.gatehill.buildbouncer.dsl.AbstractBuildBlock
import com.gatehill.buildbouncer.parser.Parser
import com.gatehill.buildbouncer.parser.inject.InstanceFactoryLocator
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class BuildAnalysisService @Inject constructor(
        private val parser: Parser,
        private val buildOutcomeService: BuildOutcomeService
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    fun analyseBuild(outcome: BuildOutcome): Analysis {
        val analysis = Analysis("Build ${outcome.build.number} on ${outcome.build.scm.branch}", logger)

        val config = parser.parse(Settings.Rules.configFile)

        val previousBuildStatus = buildOutcomeService.fetchBuildStatus(
                branchName = outcome.build.scm.branch,
                buildNumber = outcome.build.number - 1
        )

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> {
                logger.info("Build passed: $outcome")
                invoke(outcome, analysis, config.bodyHolder.buildPassed)

                if (previousBuildStatus == BuildStatus.FAILED) {
                    logger.info("Build started passing: $outcome")
                    invoke(outcome, analysis, config.bodyHolder.branchStartsPassing)
                }
            }
            BuildStatus.FAILED -> {
                logger.info("Build failed: $outcome")
                invoke(outcome, analysis, config.bodyHolder.buildFailed)

                if (previousBuildStatus == BuildStatus.SUCCESS) {
                    logger.info("Build started failing: $outcome")
                    invoke(outcome, analysis, config.bodyHolder.branchStartsFailing)
                }
            }
        }

        // runs every time
        invoke(outcome, analysis, config.bodyHolder.repository)

        analysis.log("Analysis complete")
        return analysis
    }

    /**
     * Instantiate the block of type `B` and invoke the `body` on it.
     */
    private inline fun <reified B : AbstractBlock> invoke(
            outcome: BuildOutcome,
            analysis: Analysis,
            noinline body: (B.() -> Unit)?
    ) {
        body?.let {
            val block = InstanceFactoryLocator.instance<B>()

            block.analysis = analysis
            block.branchName = outcome.build.scm.branch
            when (block) {
                is AbstractBuildBlock -> block.outcome = outcome
            }

            block.body()
        }
    }
}
