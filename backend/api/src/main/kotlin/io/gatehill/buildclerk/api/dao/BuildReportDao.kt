package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import java.time.ZonedDateTime

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildReportDao : Recorded {
    fun save(report: BuildReport)
    fun hasEverSucceeded(commit: String): Boolean
    fun lastPassingCommitForBranch(branchName: String): BuildReport?
    fun countStatusForCommitOnBranch(commit: String, branchName: String, status: BuildStatus): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
    fun fetchLast(branchName: String? = null): BuildReport?
    fun list(branchName: String? = null): List<BuildReport>
    fun fetchBetween(branchName: String? = null, start: ZonedDateTime, end: ZonedDateTime): List<BuildReport>
}
