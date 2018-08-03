package com.gatehill.scmwebhook.service

import com.gatehill.scmwebhook.config.Settings
import com.gatehill.scmwebhook.model.Analysis
import com.gatehill.scmwebhook.model.BuildOutcome
import com.gatehill.scmwebhook.model.BuildStatus
import org.apache.logging.log4j.LogManager

class BuildAnalysisService(
        private val buildOutcomeService: BuildOutcomeService,
        private val buildRunnerService: BuildRunnerService,
        private val scmService: ScmService,
        private val notificationService: NotificationService
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    suspend fun analyseBuild(outcome: BuildOutcome) {
        val analysis = Analysis("Build", logger)

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> logger.info("No action required for build: $outcome")
            BuildStatus.FAILED -> analyseFailedBuild(outcome, analysis)
        }

        if (analysis.isNotEmpty()) {
            notificationService.notify(analysis)
        }
    }

    private suspend fun analyseFailedBuild(outcome: BuildOutcome, analysis: Analysis) {
        val commit = outcome.build.scm.commit
        val branch = outcome.build.scm.branch

        analysis.log("Build ${outcome.build.number} on $branch failed")

        if (buildOutcomeService.hasEverSucceeded(commit)) {
            analysis.log("Commit $commit has previously succeeded (on any branch)")

            if (buildOutcomeService.countFailuresForCommitOnBranch(
                            commit = commit,
                            branch = branch) < Settings.Thresholds.maxFailuresForCommitOnBranch) {

                analysis.log("Rebuilding ${outcome.build.number} on $branch")
                buildRunnerService.rebuild(outcome)
            }

        } else {
            analysis.log("Commit $commit has never succeeded on any branch")
            analysis.log("Reverting commit $commit from branch $branch")
            scmService.revertCommit(commit, branch)
        }
    }
}
