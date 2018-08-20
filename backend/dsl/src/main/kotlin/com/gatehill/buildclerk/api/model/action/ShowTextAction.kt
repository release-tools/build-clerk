package com.gatehill.buildclerk.api.model.action

import com.gatehill.buildclerk.dsl.Color

/**
 * Print a message when triggered.
 */
data class ShowTextAction(
    val text: String,
    val color: Color,
    val channelName: String? = null
) : PendingAction {

    override val name = "show_text"
    override val title = "Show"

    override fun describe() = "show text '$text'${channelName?.let { " in channel: $channelName" }}"
}
