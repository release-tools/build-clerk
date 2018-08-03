package com.gatehill.scmwebhook.model

import org.apache.logging.log4j.Logger
import java.time.ZonedDateTime

class Analysis(
        val type: String,
        private val logger: Logger
) {
    val events = mutableListOf<Event>()

    fun log(message: String) {
        logger.info(message)

        events += Event(
                timestamp = ZonedDateTime.now(),
                message = message
        )
    }

    fun isNotEmpty(): Boolean = events.size > 0
}

data class Event(
        val timestamp: ZonedDateTime,
        val message: String
)
