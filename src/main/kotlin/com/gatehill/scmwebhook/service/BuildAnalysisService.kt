package com.gatehill.scmwebhook.service

import com.gatehill.scmwebhook.model.BuildOutcome
import com.gatehill.scmwebhook.model.BuildStatus
import org.apache.logging.log4j.LogManager

class BuildAnalysisService {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    suspend fun analyseBuild(outcome: BuildOutcome) {
        when (outcome.build.status) {
            BuildStatus.SUCCESS -> logger.info("No action required for build: $outcome")
            BuildStatus.FAILED -> {
                // TODO check if requires remediation
            }
        }
    }
}
