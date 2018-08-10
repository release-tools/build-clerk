package com.gatehill.buildbouncer.service.notify.slack

import com.gatehill.buildbouncer.model.slack.SlackMessage
import com.gatehill.buildbouncer.util.jsonMapper
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

    internal fun sendMessage(message: SlackMessage) {
        @Suppress("UNCHECKED_CAST")
        val params = jsonMapper.convertValue(message, Map::class.java) as Map<String, *>

        logger.info("Forwarding message to channel '${message.channel}': $params")

        slackApiService.invokeSlackCommand<Map<String, Any>>(
                commandName = "chat.postMessage",
                params = params,
                bodyMode = SlackApiService.BodyMode.JSON
        )
    }
}
