package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.BuildStatus
import org.apache.logging.log4j.LogManager

/**
 * Stores build outcomes and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildOutcomeService {
    private val logger = LogManager.getLogger(BuildOutcomeService::class.java)
    private val store = mutableListOf<BuildOutcome>()

    fun updateStatus(buildOutcome: BuildOutcome) {
        logger.info("Updating status for branch: ${buildOutcome.build.scm.branch} to: ${buildOutcome.build.status}")
        store += buildOutcome
    }

    fun fetchStatus(branchName: String): BuildOutcome? = store.asReversed().firstOrNull { outcome ->
        outcome.build.scm.branch == branchName
    }

    fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any { outcome ->
        outcome.build.scm.commit == commit && outcome.build.status == BuildStatus.SUCCESS
    }

    fun lastPassingCommitForBranch(branchName: String): BuildOutcome? = store.asReversed().find { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.status == BuildStatus.SUCCESS
    }

    fun countFailuresForCommitOnBranch(commit: String, branch: String): Int = store.asReversed().count { outcome ->
        outcome.build.scm.commit == commit && outcome.build.scm.branch == branch && outcome.build.status == BuildStatus.FAILED
    }
}
