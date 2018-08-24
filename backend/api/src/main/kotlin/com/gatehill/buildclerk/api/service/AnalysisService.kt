package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.api.model.analysis.Analysis

interface AnalysisService {
    fun analyseBuild(report: BuildReport): Analysis
    fun analysePullRequest(mergeEvent: PullRequestMergedEvent, currentBranchStatus: BuildStatus): Analysis
}
