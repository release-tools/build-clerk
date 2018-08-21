package com.gatehill.buildclerk.service.builder

import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.service.BuildReportService
import com.gatehill.buildclerk.config.Settings
import com.gatehill.buildclerk.service.AnalysisService
import kotlinx.coroutines.experimental.launch
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Records build events and triggers analysis.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildEventService @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService
) {
    private val logger = LogManager.getLogger(BuildEventService::class.java)

    fun checkBuildReport(buildReport: BuildReport) {
        val branchName = buildReport.build.scm.branch

        if (Settings.EventFilter.branchNames.isNotEmpty() &&
            Settings.EventFilter.branchNames.none { it.matches(branchName) }
        ) {
            logger.info("Ignoring build $buildReport because branch name: $branchName does not match filter")
            return
        }

        launch {
            try {
                buildReportService.record(buildReport)
                analysisService.analyseBuild(buildReport)
            } catch (e: Exception) {
                logger.error("Error handling build report: $buildReport", e)
            }
        }
    }
}
