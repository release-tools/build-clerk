package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.RevertPendingAction
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.api.service.BuildRunnerService

/**
 * Configuration file wrapper.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigWrapper(
        private val block: ConfigWrapper.() -> Unit
) {
    lateinit var buildOutcomeService: BuildOutcomeService
    lateinit var buildRunnerService: BuildRunnerService
    lateinit var analysis: Analysis
    lateinit var buildOutcome: BuildOutcome

    fun build(block: BuildBlock.() -> Unit): Unit =
            BuildBlock(buildOutcomeService, buildRunnerService, analysis, buildOutcome).block()

    fun repository(block: RepositoryBlock.() -> Unit): Unit = RepositoryBlock().block()

    fun eval() {
        block()
    }
}

fun config(block: ConfigWrapper.() -> Unit): ConfigWrapper = ConfigWrapper(block)

class BuildBlock(
        private val buildOutcomeService: BuildOutcomeService,
        private val buildRunnerService: BuildRunnerService,
        private var analysis: Analysis,
        var outcome: BuildOutcome
) {

    fun log(message: String) = analysis.log(message)

    fun hasEverSucceeded(): Boolean =
            buildOutcomeService.hasEverSucceeded(outcome.build.scm.commit)

    fun lastPassingCommitForBranch(): BuildOutcome? =
            buildOutcomeService.lastPassingCommitForBranch(outcome.build.scm.branch)

    fun countFailuresForCommitOnBranch(): Int =
            buildOutcomeService.countFailuresForCommitOnBranch(outcome.build.scm.commit, outcome.build.scm.branch)

    fun rebuild() = buildRunnerService.rebuild(outcome)

    fun revertCommit() {
        analysis.recommend(RevertPendingAction(
                commit = outcome.build.scm.commit,
                branch = outcome.build.scm.branch
        ))
    }
}

class RepositoryBlock
