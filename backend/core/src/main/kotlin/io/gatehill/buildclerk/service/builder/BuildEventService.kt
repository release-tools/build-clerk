package io.gatehill.buildclerk.service.builder

import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.service.message.BranchNotificationService
import kotlinx.coroutines.experimental.launch
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Records build events, triggers analyses and sends notifications.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildEventService @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService,
    private val branchNotificationService: BranchNotificationService,
    private val notificationService: NotificationService
) {
    private val logger = LogManager.getLogger(BuildEventService::class.java)

    fun checkBuildReport(buildReport: BuildReport) {
        analyseBuild(buildReport)
        sendNotifications(buildReport)
    }

    private fun analyseBuild(buildReport: BuildReport) {
        val branchName = buildReport.build.scm.branch
        if (Settings.EventFilter.branchNames.isEmpty() ||
            Settings.EventFilter.branchNames.any { it.matches(branchName) }
        ) {
            launch {
                try {
                    buildReportService.record(buildReport)
                    analysisService.analyseBuild(buildReport)
                } catch (e: Exception) {
                    logger.error("Error handling build report: $buildReport", e)
                }
            }
        } else {
            logger.info("Ignoring build $buildReport because branch name: $branchName does not match filter")
        }
    }

    private fun sendNotifications(buildReport: BuildReport) {
        val branchName = buildReport.build.scm.branch
        launch {
            val matches = branchNotificationService.checkForMatches(branchName)
            if (matches.isNotEmpty()) {
                logger.info("Sending ${matches.size} notifications for branch: $branchName")
                val description = mutableListOf(analysisService.buildShortDescription(buildReport))
                description += analysisService.performBasicBuildAnalysis(buildReport)

                matches.forEach { match ->
                    try {
                        logger.debug("Notifying user ${match.userId} on channel ${match.channel} about build on branch: $branchName")
                        notificationService.notify(match.channel, description.joinToString(" "))
                    } catch (e: Exception) {
                        logger.warn("Error notifying user ${match.userId} on channel ${match.channel} about build on branch: $branchName - continuing", e)
                    }
                }
            } else {
                logger.info("No notifications required for branch: $branchName")
            }
        }
    }
}
