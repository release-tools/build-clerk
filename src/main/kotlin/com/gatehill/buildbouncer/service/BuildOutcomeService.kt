package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.BuildStatus
import org.apache.logging.log4j.LogManager

interface BuildOutcomeService {
    fun updateStatus(buildOutcome: BuildOutcome)
    fun fetchStatus(branchName: String): BuildOutcome?
    fun hasEverSucceeded(commit: String): Boolean
    fun lastPassingCommitForBranch(branchName: String): BuildOutcome?
    fun countFailuresForCommitOnBranch(commit: String, branch: String): Int
}

class BuildOutcomeServiceImpl : BuildOutcomeService {
    private val logger = LogManager.getLogger(BuildOutcomeService::class.java)
    private val store = mutableListOf<BuildOutcome>()

    override fun updateStatus(buildOutcome: BuildOutcome) {
        logger.info("Updating status for branch: ${buildOutcome.build.scm.branch} to: ${buildOutcome.build.status}")
        store += buildOutcome
    }

    override fun fetchStatus(branchName: String): BuildOutcome? = store.asReversed().firstOrNull { outcome ->
        outcome.build.scm.branch == branchName
    }

    override fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any { outcome ->
        outcome.build.scm.commit == commit && outcome.build.status == BuildStatus.SUCCESS
    }

    override fun lastPassingCommitForBranch(branchName: String): BuildOutcome? = store.asReversed().find { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.status == BuildStatus.SUCCESS
    }

    override fun countFailuresForCommitOnBranch(commit: String, branch: String): Int = store.asReversed().count { outcome ->
        outcome.build.scm.commit == commit && outcome.build.scm.branch == branch && outcome.build.status == BuildStatus.FAILED
    }
}
