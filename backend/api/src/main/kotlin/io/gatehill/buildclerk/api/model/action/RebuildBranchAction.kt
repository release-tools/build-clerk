package io.gatehill.buildclerk.api.model.action

import io.gatehill.buildclerk.api.model.BuildReport

data class RebuildBranchAction(
    val report: BuildReport
) : PendingAction {

    override val name = "rebuild"
    override val title = "Rebuild branch"
    override val exclusive = false

    override fun describe() = "rebuild branch ${report.build.scm.branch}"
}
