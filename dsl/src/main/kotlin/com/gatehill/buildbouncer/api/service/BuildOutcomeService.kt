package com.gatehill.buildbouncer.api.service

import com.gatehill.buildbouncer.api.model.BuildOutcome

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
}
