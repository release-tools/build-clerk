package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.ReportSpan
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.model.pr.PullRequestEventType
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.PullRequestModifiedEvent
import java.time.ZonedDateTime

/**
 * Performs analyses of certain events.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface AnalysisService {
    fun analyseBuild(report: BuildReport): Analysis
    fun buildShortDescription(report: BuildReport): String
    fun analyseModifiedPullRequest(prEvent: PullRequestModifiedEvent, eventType: PullRequestEventType): Analysis
    fun analyseMergedPullRequest(prEvent: PullRequestMergedEvent, currentBranchStatus: BuildStatus): Analysis
    fun performBasicBuildAnalysis(report: BuildReport): List<String>
    fun analyseReportSpan(branchName: String?, start: ZonedDateTime, end: ZonedDateTime): ReportSpan
}
