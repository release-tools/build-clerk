package com.gatehill.buildbouncer.model

import org.apache.logging.log4j.Logger
import java.time.ZonedDateTime

class Analysis(
        private val name: String,
        private val logger: Logger
) {
    private val events = mutableListOf<Event>()

    fun log(message: String) {
        logger.debug(message)

        events += Event(
                timestamp = ZonedDateTime.now(),
                message = message
        )
    }

    fun isNotEmpty(): Boolean = events.size > 0

    override fun toString(): String {
        return """-----------------------------------------
Analysis of $name
${events.joinToString("\n")}
-----------------------------------------"""
    }
}

data class Event(
        val timestamp: ZonedDateTime,
        val message: String
)
