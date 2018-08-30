package io.gatehill.buildclerk.api.model.analysis

import io.gatehill.buildclerk.api.model.action.PendingActionSet
import org.apache.logging.log4j.Logger
import java.time.ZonedDateTime

class Analysis(
    private val logger: Logger,
    val name: String,
    val branch: String,
    val user: String? = null,
    val url: String? = null
) {
    val perform = PendingActionSet()
    val suggested = PendingActionSet()
    var publishConfig: PublishConfig? = null
    private val events = mutableListOf<AnalysisEvent>()

    fun log(message: String) {
        logger.debug(message)

        events += AnalysisEvent(
            timestamp = ZonedDateTime.now(),
            message = message
        )
    }

    fun isNotEmpty(): Boolean = events.isNotEmpty() || perform.actions.isNotEmpty() || suggested.actions.isNotEmpty()

    fun describeEvents(): String = events.joinToString("\n") { it.message }

    override fun toString() = """
----- Analysis of $name -----
${events.joinToString("\n")}
Performed actions:
${perform.actions.joinToString("\n")}
Suggested actions:
${suggested.actions.joinToString("\n")}
""".trimMargin()
}
