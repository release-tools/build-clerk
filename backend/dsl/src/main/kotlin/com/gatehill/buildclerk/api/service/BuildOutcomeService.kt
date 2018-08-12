package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.BuildOutcome
import com.gatehill.buildclerk.api.model.BuildStatus

/**
 * Stores build outcomes and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildOutcomeService {
    fun updateStatus(buildOutcome: BuildOutcome)
    fun fetchStatus(branchName: String): BuildOutcome?
    fun hasEverSucceeded(commit: String): Boolean
    fun lastPassingCommitForBranch(branchName: String): BuildOutcome?
    fun countFailuresForCommitOnBranch(commit: String, branch: String): Int
    fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus?
    fun countConsecutiveFailuresOnBranch(branchName: String): Int
}