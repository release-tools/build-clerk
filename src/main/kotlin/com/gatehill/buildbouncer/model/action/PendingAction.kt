package com.gatehill.buildbouncer.model.action

import java.util.UUID

interface PendingAction {
    val id: String

    fun describe(): String
}

abstract class BasePendingAction : PendingAction {
    override val id = UUID.randomUUID().toString()

    override fun toString(): String {
        return "${this::class.java.simpleName}(${this.id})"
    }
}
