package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildReportService {
    fun record(buildReport: BuildReport)
    fun fetchLastBuildForBranch(branchName: String): BuildReport?
    fun hasEverSucceeded(commit: String): Boolean
    fun lastPassingBuildForBranch(branchName: String): BuildReport?
    fun countStatusForCommitOnBranch(commit: String, branch: String, status: BuildStatus): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
    fun countReports(): Int
}
