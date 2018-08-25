package io.gatehill.buildclerk.api.model.analysis

import io.gatehill.buildclerk.api.model.action.PendingAction
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
    val actionSet = PendingActionSet()
    var postConfig: PostConfig? = null
    private val events = mutableListOf<AnalysisEvent>()

    fun log(message: String) {
        logger.debug(message)

        events += AnalysisEvent(
            timestamp = ZonedDateTime.now(),
            message = message
        )
    }

    fun recommend(pendingAction: PendingAction) {
        if (!actionSet.actions.contains(pendingAction)) {
            logger.debug("Recommending action: ${pendingAction.describe()}")
            actionSet.actions += pendingAction
        }
    }

    fun isNotEmpty(): Boolean = events.isNotEmpty() || actionSet.actions.isNotEmpty()

    fun describeEvents(): String {
        return """
${events.joinToString("\n") { it.message }}
""".trimMargin()
    }

    override fun toString(): String {
        return """
----- Analysis of $name -----
${events.joinToString("\n")}
Recommended actions:
${actionSet.actions.joinToString("\n")}
""".trimMargin()
    }
}