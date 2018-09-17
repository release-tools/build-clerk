package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import java.time.ZonedDateTime

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildReportService : Recorded {
    fun record(buildReport: BuildReport)
    fun hasEverSucceeded(commit: String): Boolean
    fun fetchLastPassingBuildForBranch(branchName: String): BuildReport?
    fun countStatusForCommitOnBranch(commit: String, branch: String, status: BuildStatus): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
    fun fetchLastReport(branchName: String? = null): BuildReport?
    fun fetchReports(branchName: String? = null): List<BuildReport>

    /**
     * @param start inclusive
     * @param end exclusive
     */
    fun fetchReportsBetween(branchName: String? = null, start: ZonedDateTime, end: ZonedDateTime): List<BuildReport>

    /**
     * Find any build report for this branch with a build number higher than `buildNumber`.
     *
     * Note, there is no guarantee that the build
     * will be the next consecutive build, only that it has a higher build number.
     *
     * @return a build report, if one or more is found
     */
    fun findHigherBuild(branchName: String, buildNumber: Int): BuildReport?
}
