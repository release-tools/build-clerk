package io.gatehill.buildclerk.service.builder

import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.util.Color
import io.gatehill.buildclerk.service.message.BranchNotificationService
import io.gatehill.buildclerk.supervisedDefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
) : CoroutineScope by supervisedDefaultCoroutineScope {
    private val logger = LogManager.getLogger(BuildEventService::class.java)

    fun checkBuildReport(buildReport: BuildReport) {
        launch {
            try {
                analyseBuild(buildReport)
                sendDirectNotifications(buildReport)
            } catch (e: Exception) {
                logger.error("Error handling build report: $buildReport", e)
            }
        }
    }

    private fun analyseBuild(buildReport: BuildReport) {
        val branchName = buildReport.build.scm.branch
        if (Settings.EventFilter.branchNames.isEmpty() ||
            Settings.EventFilter.branchNames.any { it.matches(branchName) }
        ) {
            buildReportService.record(buildReport)
            analysisService.analyseBuild(buildReport)
        } else {
            logger.info("Ignoring build $buildReport because branch name: $branchName does not match filter")
        }
    }

    private fun sendDirectNotifications(buildReport: BuildReport) {
        val branchName = buildReport.build.scm.branch
        val matches = branchNotificationService.checkForMatches(branchName)
        if (matches.isNotEmpty()) {
            logger.info("Sending ${matches.size} notifications for branch: $branchName")

            val description = mutableListOf(analysisService.buildShortDescription(buildReport))
            description += analysisService.performBasicBuildAnalysis(buildReport)

            val colour = when(buildReport.build.status) {
                BuildStatus.SUCCESS -> Color.GREEN
                BuildStatus.FAILED -> Color.RED
                else -> Color.BLACK
            }

            matches.forEach { match ->
                try {
                    logger.debug("Notifying user ${match.userId} on channel ${match.channel} about build on branch: $branchName")
                    notificationService.notify(match.channel, description.joinToString("\n"), colour.hexCode)
                } catch (e: Exception) {
                    logger.warn(
                        "Error notifying user ${match.userId} on channel ${match.channel} about build on branch: $branchName - continuing", e
                    )
                }
            }
        } else {
            logger.info("No notifications required for branch: $branchName")
        }
    }
}
