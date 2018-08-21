package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus

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
    fun countFailuresForCommitOnBranch(commit: String, branch: String): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
}
