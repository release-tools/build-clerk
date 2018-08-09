package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.parser.inject.InstanceFactoryLocator
import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.dsl.BaseBlock
import com.gatehill.buildbouncer.parser.Parser
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
                    invoke(outcome, analysis, config.bodyHolder.branchStartsPassing)
                }
            }
            BuildStatus.FAILED -> {
                logger.info("Build failed: $outcome")
                invoke(outcome, analysis, config.bodyHolder.buildFailed)

                if (previousBuildStatus == BuildStatus.SUCCESS) {
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
    private inline fun <reified B : Any> invoke(
        outcome: BuildOutcome,
        analysis: Analysis,
        noinline body: (B.() -> Unit)?
    ) {
        body?.let {
            val block = InstanceFactoryLocator.instance<B>()
            when (block) {
                is BaseBlock -> {
                    block.outcome = outcome
                    block.analysis = analysis
                }
            }
            block.body()
        }
    }
}
