package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.BuildStatus
import kotlinx.coroutines.experimental.async
import org.apache.logging.log4j.LogManager

class BuildOutcomeService(
    private val buildAnalysisService: BuildAnalysisService
) {
    private val logger = LogManager.getLogger(BuildOutcomeService::class.java)
    private val store = mutableListOf<BuildOutcome>()

    fun updateStatus(buildOutcome: BuildOutcome) {
        logger.info("Updating status for branch: ${buildOutcome.build.scm.branch} to: ${buildOutcome.build.status}")
        store += buildOutcome

        async {
            buildAnalysisService.analyseBuild(buildOutcome)
        }
    }

    suspend fun fetchStatus(branchName: String): BuildOutcome? = store.asReversed().firstOrNull { outcome ->
        outcome.build.scm.branch == branchName
    }

    suspend fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any { outcome ->
        outcome.build.scm.commit == commit && outcome.build.status == BuildStatus.SUCCESS
    }

    suspend fun lastPassingCommitForBranch(branchName: String): BuildOutcome? = store.asReversed().find { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.status == BuildStatus.SUCCESS
    }

    fun countFailuresForCommitOnBranch(commit: String, branch: String) = store.asReversed().count { outcome ->
        outcome.build.scm.commit == commit && outcome.build.scm.branch == branch && outcome.build.status == BuildStatus.FAILED
    }
}
