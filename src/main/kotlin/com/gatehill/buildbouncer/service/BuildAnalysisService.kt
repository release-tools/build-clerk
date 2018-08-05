package com.gatehill.buildbouncer.service

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.model.Analysis
import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.BuildStatus
import com.gatehill.buildbouncer.model.action.RevertPendingAction
import com.gatehill.buildbouncer.service.runner.BuildRunnerService
import org.apache.logging.log4j.LogManager

class BuildAnalysisService(
        private val buildOutcomeService: BuildOutcomeService,
        private val buildRunnerService: BuildRunnerService
) {
    private val logger = LogManager.getLogger(BuildAnalysisService::class.java)

    fun analyseBuild(outcome: BuildOutcome): Analysis {
        val analysis = Analysis("Build ${outcome.build.number} on ${outcome.build.scm.branch}", logger)

        when (outcome.build.status) {
            BuildStatus.SUCCESS -> logger.info("No action required for successful build: $outcome")
            BuildStatus.FAILED -> analyseFailedBuild(outcome, analysis)
        }

        analysis.log("Analysis complete")
        return analysis
    }

    private fun analyseFailedBuild(outcome: BuildOutcome, analysis: Analysis) {
        val commit = outcome.build.scm.commit
        val branch = outcome.build.scm.branch

        analysis.log("Analysing failed build")

        if (buildOutcomeService.hasEverSucceeded(commit)) {
            analysis.log("Commit $commit has previously succeeded (on at least 1 branch)")

            val failuresForCommitOnBranch = buildOutcomeService.countFailuresForCommitOnBranch(
                    commit = commit,
                    branch = branch)

            analysis.log("Commit has failed $failuresForCommitOnBranch time on $branch")

            if (failuresForCommitOnBranch < Settings.Thresholds.maxFailuresForCommitOnBranch) {
                analysis.log("Rebuilding ${outcome.build.number} on $branch")
                buildRunnerService.rebuild(outcome)
            } else {
                revertCommit(commit, branch, analysis)
            }

        } else {
            analysis.log("Commit $commit has never succeeded on any branch")
            revertCommit(commit, branch, analysis)
        }
    }

    private fun revertCommit(commit: String, branch: String, analysis: Analysis) {
        analysis.recommend(RevertPendingAction(
                commit = commit,
                branch = branch
        ))
    }
}