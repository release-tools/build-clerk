package com.gatehill.buildbouncer.model.action

data class RevertPendingAction(
        val commit: String,
        val branch: String
) : PendingAction {

    override val name = "revert"
    override val title = "Revert"

    override fun describe() = "revert commit $commit from branch $branch"
}
