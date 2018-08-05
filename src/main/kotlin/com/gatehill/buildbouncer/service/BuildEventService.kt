package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.service.notify.NotificationService
import kotlinx.coroutines.experimental.async

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

    fun handle(buildOutcome: BuildOutcome) {
        buildOutcomeService.updateStatus(buildOutcome)

        async {
            val analysis = buildAnalysisService.analyseBuild(buildOutcome)

            if (analysis.isNotEmpty()) {
                pendingActionService.enqueue(analysis.actions)
                notificationService.notify(analysis)
            }
        }
    }
}
