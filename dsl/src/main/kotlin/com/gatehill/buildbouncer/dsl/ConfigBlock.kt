package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.action.LockBranchAction
import com.gatehill.buildbouncer.api.model.action.RebuildBranchAction
import com.gatehill.buildbouncer.api.model.action.RevertAction
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.api.service.NotificationService

/**
 * Configuration file wrapper.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigBlock(
    val body: ConfigBlock.() -> Unit
) {
    val bodyHolder = BodyHolder()

    fun buildPassed(body: BuildPassedBlock.() -> Unit) {
        bodyHolder.buildPassed = body
    }

    fun buildFailed(body: BuildFailedBlock.() -> Unit) {
        bodyHolder.buildFailed = body
    }

    fun branchStartsPassing(body: BuildHealthyBlock.() -> Unit) {
        bodyHolder.branchStartsPassing = body
    }

    fun branchStartsFailing(body: BuildFailingBlock.() -> Unit) {
        bodyHolder.branchStartsFailing = body
    }

    fun repository(body: RepositoryBlock.() -> Unit) {
        bodyHolder.repository = body
    }
}

class BodyHolder {
    lateinit var buildPassed: BuildPassedBlock.() -> Unit
    lateinit var buildFailed: BuildFailedBlock.() -> Unit
    lateinit var branchStartsPassing: BuildHealthyBlock.() -> Unit
    lateinit var branchStartsFailing: BuildFailingBlock.() -> Unit
    lateinit var repository: RepositoryBlock.() -> Unit
}

abstract class BaseBlock(
    private val notificationService: NotificationService,
    private val buildOutcomeService: BuildOutcomeService
) {
    lateinit var outcome: BuildOutcome
    lateinit var analysis: Analysis

    fun log(message: String) = analysis.log(message)

    fun hasEverSucceeded(): Boolean =
        buildOutcomeService.hasEverSucceeded(outcome.build.scm.commit)

    fun lastPassingCommitForBranch(): BuildOutcome? =
        buildOutcomeService.lastPassingCommitForBranch(outcome.build.scm.branch)

    fun countFailuresForCommitOnBranch(): Int =
        buildOutcomeService.countFailuresForCommitOnBranch(outcome.build.scm.commit, outcome.build.scm.branch)

    fun rebuildBranch() {
        analysis.recommend(
            RebuildBranchAction(outcome)
        )
    }

    fun revertCommit() {
        analysis.recommend(
            RevertAction(
                commit = outcome.build.scm.commit,
                branch = outcome.build.scm.branch
            )
        )
    }

    fun lockBranch() {
        analysis.recommend(
            LockBranchAction(outcome.build.scm.branch)
        )
    }

    fun notifyChannel(channelName: String, message: String, color: String = "#000000") {
        notificationService.notify(channelName, message, color)
    }

    fun notifyChannel(channelName: String, analysis: Analysis, color: String = "#000000") {
        notificationService.notify(channelName, analysis, color)
    }
}

class BuildPassedBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService
) : BaseBlock(notificationService, buildOutcomeService)

class BuildFailedBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService
) : BaseBlock(notificationService, buildOutcomeService)

class BuildHealthyBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService
) : BaseBlock(notificationService, buildOutcomeService)

class BuildFailingBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService
) : BaseBlock(notificationService, buildOutcomeService)

class RepositoryBlock

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
