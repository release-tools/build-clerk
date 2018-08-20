package com.gatehill.buildclerk.api.model.action

import com.gatehill.buildclerk.toShortCommit

data class RevertCommitAction(
    val commit: String,
    val branch: String
) : PendingAction {

    override val name = "revert"
    override val title = "Revert"

    override fun describe() = "revert commit ${toShortCommit(commit)} from branch $branch"
}
