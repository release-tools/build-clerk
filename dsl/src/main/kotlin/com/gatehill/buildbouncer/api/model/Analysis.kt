package com.gatehill.buildbouncer.api.model

import org.apache.logging.log4j.Logger
import java.time.ZonedDateTime

class Analysis(
        private val name: String,
        private val logger: Logger
) {
    val actionSet = PendingActionSet()
    private val events = mutableListOf<Event>()

    fun log(message: String) {
        logger.debug(message)

        events += Event(
                timestamp = ZonedDateTime.now(),
                message = message
        )
    }

    fun recommend(pendingAction: PendingAction) {
        log("Recommending action: ${pendingAction.describe()}")
        actionSet.actions += pendingAction
    }

    fun isNotEmpty(): Boolean = events.isNotEmpty() || actionSet.actions.isNotEmpty()

    fun describeEvents(): String {
        return """
Analysis of $name:
${events.joinToString("\n") { it.message }}
""".trimMargin()
    }

    override fun toString(): String {
        return """
----- Analysis of $name -----
${events.joinToString("\n")}
Pending actions:
${actionSet.actions.joinToString("\n")}
""".trimMargin()
    }
}

data class Event(
        val timestamp: ZonedDateTime,
        val message: String
)
