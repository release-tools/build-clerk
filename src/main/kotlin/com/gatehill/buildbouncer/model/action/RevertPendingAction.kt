package com.gatehill.buildbouncer.model.action

class RevertPendingAction(
        val commit: String,
        val branch: String
) : BasePendingAction() {

    override fun describe() = "Revert commit $commit from branch $branch"

    override fun toString(): String {
        return "RevertPendingAction(id='$id', commit='$commit', branch='$branch')"
    }
}
