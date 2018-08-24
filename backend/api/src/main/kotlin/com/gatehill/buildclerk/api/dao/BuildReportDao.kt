package com.gatehill.buildclerk.api.dao

import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildReportDao {
    fun save(report: BuildReport)
    fun fetchLastBuildForBranch(branchName: String): BuildReport?
    fun hasEverSucceeded(commit: String): Boolean
    fun lastPassingCommitForBranch(branchName: String): BuildReport?
    fun countStatusForCommitOnBranch(commit: String, branchName: String, status: BuildStatus): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
}
