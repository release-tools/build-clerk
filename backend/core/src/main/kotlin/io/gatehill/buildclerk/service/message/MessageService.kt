package io.gatehill.buildclerk.service.message

import com.fasterxml.jackson.module.kotlin.readValue
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.model.message.EventApiEvent
import io.gatehill.buildclerk.model.message.EventCallbackWrapper
import io.gatehill.buildclerk.model.message.UrlVerificationEvent
import io.gatehill.buildclerk.util.VersionUtil
import io.gatehill.buildclerk.util.jsonMapper
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class MessageService @Inject constructor(
    private val notificationService: NotificationService,
    private val branchNotificationService: BranchNotificationService
) {
    private val logger = LogManager.getLogger(MessageService::class.java)

    fun parse(rawEvent: String): String? {
        logger.trace("Received Slack event: $rawEvent")
        val event = jsonMapper.readValue<EventApiEvent>(rawEvent)

        when (event.type) {
            "url_verification" -> {
                val urlVerification = jsonMapper.readValue<UrlVerificationEvent>(rawEvent)
                logger.debug("Handling Slack URL verification request with challenge: ${urlVerification.challenge}")
                return urlVerification.challenge
            }
            "event_callback" -> {
                val callback = jsonMapper.readValue<EventCallbackWrapper>(rawEvent)
                val channelType = callback.event["channel_type"] as? String
                val botId = callback.event["bot_id"] as? String

                if (channelType == "im") {
                    // ignore own IMs
                    if (botId.isNullOrBlank()) {
                        val userId = callback.event["user"] as String
                        val channel = callback.event["channel"] as String
                        val text = callback.event["text"] as String
                        handleIm(userId, channel, text)
                    } else {
                        logger.trace("Ignoring own IM")
                    }
                } else {
                    logger.debug("Ignoring Slack event callback with unsupported channel type: $channelType")
                }
            }
            else -> logger.debug("Ignoring unsupported Slack event type: ${event.type}")
        }
        return null
    }

    private fun handleIm(userId: String, channel: String, text: String) {
        logger.debug("Handling IM received from user: $userId: $text")
        val normalised = normaliseMessage(text)

        when {
            normalised.isEmpty() -> {
                logger.warn("Could not parse IM from user: $userId: $text")
                respondMessageParsingFailure(channel)
            }

            normalised.equals("help", ignoreCase = true) -> respondUsage(channel)

            else -> {
                var responded = false

                regexMatchWithBranch(normalised, "notify me about (?<branch>.+)") { branch ->
                    branch?.let {
                        branchNotificationService.registerNotificationForUser(userId, channel, branch)
                        notificationService.notify(channel, "You'll be notified about '$branch'.")
                        responded = true
                    } ?: respondMessageParsingFailure(channel)
                }

                regexMatchWithBranch(normalised, "don('?)t notify me about (?<branch>.+)") { branch ->
                    branch?.let {
                        branchNotificationService.unregisterNotificationForUser(userId, branch)
                        notificationService.notify(channel, "You won't be notified about '$branch'.")
                        responded = true
                    } ?: respondMessageParsingFailure(channel)
                }

                regexMatch(normalised, "don('?)t notify me") {
                    branchNotificationService.unregisterAllNotificationsForUser(userId)
                    notificationService.notify(channel, "You won't be notified about any branches.")
                    responded = true
                }

                regexMatch(normalised, "list") {
                    val notificationsForUser = branchNotificationService.fetchNotificationsForUser(userId)
                    if (notificationsForUser.isEmpty()) {
                        notificationService.notify(channel, "You won't get any notifications.")
                    } else {
                        val notifications = notificationsForUser.joinToString(separator = "\n", prefix = "\n") { "• ${it.branch}" }
                        notificationService.notify(channel, "You'll get notifications for: $notifications")
                    }
                    responded = true
                }

                if (!responded) {
                    respondMessageParsingFailure(channel)
                }
            }
        }
    }

    private fun respondMessageParsingFailure(channel: String) {
        notificationService.notify(
            channel, """
            Sorry, I didn't understand. Try typing _help_ for more information.
        """.trimIndent()
        )
    }

    private fun respondUsage(channel: String) {
        notificationService.notify(
            channel, """
            Hi! You can try the following:
            • _notify me about <branch name>_ - receive a message when a build occurs for a branch matching a string
            • _don't notify me about <branch name>_ - don't receive a message when a build occurs for a branch matching a string
            • _don't notify me_ - don't receive any messages about branch builds
            • _list_ - list your active branch notifications
            • _help_ - display this message

            _(Build Clerk version ${VersionUtil.version} - http://buildclerk.org)_
        """.trimIndent()
        )
    }

    private fun regexMatch(
        text: String,
        pattern: String,
        block: () -> Unit
    ) {
        if (Regex(pattern, RegexOption.IGNORE_CASE).matches(text)) {
            block()
        }
    }

    private fun regexMatchWithBranch(
        text: String,
        pattern: String,
        block: (branch: String?) -> Unit
    ) {
        val result = Regex(pattern, RegexOption.IGNORE_CASE).matchEntire(text)
        result?.let {
            block(result.groups["branch"]?.value)
        }
    }

    private fun normaliseMessage(text: String): String {
        return text.trim().split(Regex("\\s+")).joinToString(" ")
    }
}
