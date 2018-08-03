package com.gatehill.scmwebhook.service

import com.gatehill.scmwebhook.model.BuildOutcome
import org.apache.logging.log4j.LogManager

class BranchStatusService {
    private val logger = LogManager.getLogger(BranchStatusService::class.java)
    private val store = mutableMapOf<String, BuildOutcome>()

    fun updateStatus(buildOutcome: BuildOutcome) {
        val branchName = buildOutcome.build.scm.branch
        logger.info("Updating status for branch: $branchName to: ${buildOutcome.build.status}")
        store[branchName] = buildOutcome
    }

    suspend fun fetchStatus(branchName: String): BuildOutcome? = store[branchName]
}
