package io.gatehill.buildclerk.service.notify.slack

import io.gatehill.buildclerk.model.slack.SlackMessage
import io.gatehill.buildclerk.util.jsonMapper
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
        checkChannel(message)

        val params = convertMessageToMap(message)
        logger.info("Sending message to channel '${message.channel}': $params")

        slackApiService.invokeSlackCommand<Map<String, Any>>(
            commandName = "chat.postMessage",
            params = params,
            bodyMode = SlackApiService.BodyMode.JSON
        )
    }

    fun updateMessage(message: SlackMessage) {
        checkChannel(message)

        val params = convertMessageToMap(message)
        logger.info("Updating message in channel '${message.channel}': $params")

        slackApiService.invokeSlackCommand<Map<String, Any>>(
            commandName = "chat.update",
            params = params,
            bodyMode = SlackApiService.BodyMode.JSON
        )
    }

    private fun checkChannel(message: SlackMessage) {
        checkNotNull(message.channel) { "Missing channel on message" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertMessageToMap(message: SlackMessage): Map<String, *> =
        jsonMapper.convertValue(message, Map::class.java) as Map<String, *>
}
