package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.model.Analysis
import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.BuildStatus
import org.apache.logging.log4j.LogManager

class BuildAnalysisService(
        private val buildOutcomeService: BuildOutcomeService,
        private val buildRunnerService: BuildRunnerService,
        private val scmService: ScmService,
        private val notificationService: NotificationService
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    suspend fun analyseBuild(outcome: BuildOutcome) {
        val analysis = Analysis("Build ${outcome.build.number} on ${outcome.build.scm.branch}", logger)

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> logger.info("No action required for successful build: $outcome")
            BuildStatus.FAILED -> analyseFailedBuild(outcome, analysis)
        }

        if (analysis.isNotEmpty()) {
            analysis.log("Analysis complete")
            logger.info(analysis)
            notificationService.notify(analysis)
        }
    }

    private suspend fun analyseFailedBuild(outcome: BuildOutcome, analysis: Analysis) {
        val commit = outcome.build.scm.commit
        val branch = outcome.build.scm.branch

        analysis.log("Analysing failed build")

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
