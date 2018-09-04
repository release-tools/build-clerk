package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.ReportSpan
import io.gatehill.buildclerk.api.model.analysis.Analysis
import java.time.ZonedDateTime

/**
 * Performs analyses of certain events.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface AnalysisService {
    fun analyseBuild(report: BuildReport): Analysis
    fun analysePullRequest(mergeEvent: PullRequestMergedEvent, currentBranchStatus: BuildStatus): Analysis
    fun performBasicBuildAnalysis(report: BuildReport): List<String>
    fun analyseReportSpan(branchName: String?, start: ZonedDateTime, end: ZonedDateTime): ReportSpan
}
