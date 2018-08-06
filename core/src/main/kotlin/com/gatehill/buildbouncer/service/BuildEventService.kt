package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.service.notify.NotificationService
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager

/**
 * Records build events, triggers analysis and sends notifications of pending actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildEventService(
        private val buildOutcomeService: BuildOutcomeService,
        private val buildAnalysisService: BuildAnalysisService,
        private val pendingActionService: PendingActionService,
        private val notificationService: NotificationService
) {
    private val logger = LogManager.getLogger(BuildEventService::class.java)

    fun handle(buildOutcome: BuildOutcome) {
        buildOutcomeService.updateStatus(buildOutcome)

        async {
            try {
                val analysis = buildAnalysisService.analyseBuild(buildOutcome)

                if (analysis.isNotEmpty()) {
                    pendingActionService.enqueue(analysis.actionSet)
                    notificationService.notify(analysis)
                }
            } catch (e: Exception) {
                logger.error("Error handling build outcome: $buildOutcome", e)
            }
        }
    }
}
