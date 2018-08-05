package com.gatehill.buildbouncer.model.action

interface PendingAction {
    val name: String
    val title: String

    /**
     * Should be lowercase to enable easy insertion into action sentence.
     */
    fun describe(): String
}
