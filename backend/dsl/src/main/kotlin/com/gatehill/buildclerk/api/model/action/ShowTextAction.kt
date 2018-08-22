package com.gatehill.buildclerk.api.model.action

import com.gatehill.buildclerk.dsl.Color

/**
 * Print a message when triggered.
 */
data class ShowTextAction(
        val body: String,
        override val title: String = "Show",
        val description: String? = null,
        val color: Color,
        val channelName: String? = null
) : PendingAction {

    override val name = "show_text"
    override val exclusive = false

    override fun describe() = description
            ?: "show text '$body'${channelName?.let { " in channel: $channelName" }}"
}
