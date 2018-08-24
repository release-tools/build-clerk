package com.gatehill.buildclerk.api.model.action

import com.gatehill.buildclerk.api.util.toShortCommit

data class RevertCommitAction(
    val commit: String,
    val branch: String
) : PendingAction {

    override val name = "revert"
    override val title = "Revert"
    override val exclusive = true

    override fun describe() = "revert commit ${toShortCommit(commit)} from branch $branch"
}
