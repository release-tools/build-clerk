package com.gatehill.buildbouncer.api.model.action

data class LockBranchAction(
    val branch: String
) : PendingAction {

    override val name = "lock"
    override val title = "Lock branch"

    override fun describe() = "lock branch $branch against further changes"
}
