package com.gatehill.buildbouncer.api.model.action

import com.gatehill.buildbouncer.api.model.BuildOutcome

data class RebuildBranchAction(
    val outcome: BuildOutcome
) : PendingAction {

    override val name = "rebuild"
    override val title = "Rebuild branch"

    override fun describe() = "rebuild branch ${outcome.build.scm.branch}"
}
