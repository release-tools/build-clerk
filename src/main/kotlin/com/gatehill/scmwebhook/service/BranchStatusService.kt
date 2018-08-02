package com.gatehill.scmwebhook.service

import com.gatehill.scmwebhook.model.BranchStatus
import org.apache.logging.log4j.LogManager

class BranchStatusService {
    private val logger = LogManager.getLogger(BranchStatusService::class.java)
    private val store = mutableMapOf<String, BranchStatus>()

    fun updateStatus(branchName: String, branchStatus: BranchStatus) {
        logger.info("Updating status for branch: $branchName to: $branchStatus")
        store[branchName] = branchStatus
    }

    suspend fun fetchStatus(branchName: String): BranchStatus? = store[branchName]
}
