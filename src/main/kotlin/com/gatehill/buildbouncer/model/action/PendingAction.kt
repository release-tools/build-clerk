package com.gatehill.buildbouncer.model.action

interface PendingAction {
    val name: String

    /**
     * Should be lowercase to enable easy insertion into action sentence.
     */
    fun describe(): String
}
