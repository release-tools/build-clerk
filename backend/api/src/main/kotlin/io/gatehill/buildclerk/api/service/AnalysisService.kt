package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.analysis.Analysis

interface AnalysisService {
    fun analyseBuild(report: BuildReport): Analysis
    fun analysePullRequest(
        mergeEvent: PullRequestMergedEvent,
        currentBranchStatus: BuildStatus
    ): Analysis
}
