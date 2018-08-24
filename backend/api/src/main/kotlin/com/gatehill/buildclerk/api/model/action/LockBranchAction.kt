package com.gatehill.buildclerk.api.model.action

data class LockBranchAction(
    val branch: String
) : PendingAction {

    override val name = "lock"
    override val title = "Lock branch"
    override val exclusive = false

    override fun describe() = "lock branch $branch against further changes"
}
