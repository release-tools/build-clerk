package com.gatehill.buildbouncer.service.notify.slack

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Common Slack operations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOperationsService @Inject constructor(
        private val slackApiService: SlackApiService
) {
    private val logger: Logger = LogManager.getLogger(SlackOperationsService::class.java)

    internal fun sendMessage(params: Map<String, Any?>) {
        logger.info("Forwarding message to channel '${params["channel"]}': $params")

        slackApiService.invokeSlackCommand<Map<String, Any>>(
                commandName = "chat.postMessage",
                params = params,
                bodyMode = SlackApiService.BodyMode.JSON
        )
    }
}
