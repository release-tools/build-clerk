package com.gatehill.buildbouncer.service.builder

import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.service.AnalysisService
import com.gatehill.buildbouncer.service.PendingActionService
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Records build events and triggers analysis.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildEventService @Inject constructor(
        private val buildOutcomeService: BuildOutcomeService,
        private val analysisService: AnalysisService,
        private val pendingActionService: PendingActionService
) {
    private val logger = LogManager.getLogger(BuildEventService::class.java)

    fun checkBuildOutcome(buildOutcome: BuildOutcome) {
        val branchName = buildOutcome.build.scm.branch

        Settings.EventFilter.branchName?.takeIf(String::isNotBlank)?.let { filterBranchName ->
            if (branchName != filterBranchName) {
                logger.info("Ignoring build $buildOutcome because branch name: $branchName does not match filter")
                return
            }
        }

        @Suppress("DeferredResultUnused")
        async {
            try {
                buildOutcomeService.updateStatus(buildOutcome)
                val analysis = analysisService.analyseBuild(buildOutcome)
                if (analysis.isNotEmpty()) {
                    pendingActionService.enqueue(analysis.actionSet)
                }

            } catch (e: Exception) {
                logger.error("Error handling build outcome: $buildOutcome", e)
            }
        }
    }
}
