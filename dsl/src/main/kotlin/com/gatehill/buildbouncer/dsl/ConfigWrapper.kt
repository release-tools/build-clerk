package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.InstanceFactoryLocator
import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.RevertPendingAction
import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.api.service.BuildRunnerService
import com.gatehill.buildbouncer.api.service.NotificationService

/**
 * Configuration file wrapper.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigWrapper(
    private val configBody: ConfigWrapper.() -> Unit
) {
    lateinit var analysis: Analysis
    lateinit var buildOutcome: BuildOutcome

    private lateinit var buildPassedBody: BuildPassedBlock.() -> Unit
    private lateinit var buildFailedBody: BuildFailedBlock.() -> Unit
    private lateinit var branchStartsFailingBody: BuildFailingBlock.() -> Unit
    private lateinit var branchStartsPassingBody: BuildHealthyBlock.() -> Unit
    private lateinit var repositoryBody: RepositoryBlock.() -> Unit

    fun buildPassed(body: BuildPassedBlock.() -> Unit) {
        buildPassedBody = body
    }

    fun buildFailed(body: BuildFailedBlock.() -> Unit) {
        buildFailedBody = body
    }

    fun branchStartsFailing(body: BuildFailingBlock.() -> Unit) {
        branchStartsFailingBody = body
    }

    fun branchStartsPassing(body: BuildHealthyBlock.() -> Unit) {
        branchStartsPassingBody = body
    }

    fun repository(body: RepositoryBlock.() -> Unit) {
        repositoryBody = body
    }

    fun invokeBuildPassed() {
        val block = InstanceFactoryLocator.instance<BuildPassedBlock>()
        block.buildPassedBody()
    }

    fun invokeBuildFailed() {
        val block = InstanceFactoryLocator.instance<BuildFailedBlock>()
        block.buildFailedBody()
    }

    fun invokeBranchStartsFailing() {
        val block = InstanceFactoryLocator.instance<BuildFailingBlock>()
        block.branchStartsFailingBody()
    }

    fun invokeBranchStartsPassing() {
        val block = InstanceFactoryLocator.instance<BuildHealthyBlock>()
        block.branchStartsPassingBody()
    }

    fun invokeRepository() {
        val block = InstanceFactoryLocator.instance<RepositoryBlock>()
        block.repositoryBody()
    }

    fun eval() {
        configBody()
    }
}

fun config(block: ConfigWrapper.() -> Unit): ConfigWrapper = ConfigWrapper(block)

abstract class BaseBlock(
    private val notificationService: NotificationService,
    private val buildOutcomeService: BuildOutcomeService,
    private val buildRunnerService: BuildRunnerService
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

    fun rebuild() = buildRunnerService.rebuild(outcome)

    fun revertCommit() {
        analysis.recommend(
            RevertPendingAction(
                commit = outcome.build.scm.commit,
                branch = outcome.build.scm.branch
            )
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
    buildOutcomeService: BuildOutcomeService,
    buildRunnerService: BuildRunnerService
) : BaseBlock(notificationService, buildOutcomeService, buildRunnerService)

class BuildFailedBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService,
    buildRunnerService: BuildRunnerService
) : BaseBlock(notificationService, buildOutcomeService, buildRunnerService)

class BuildHealthyBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService,
    buildRunnerService: BuildRunnerService
) : BaseBlock(notificationService, buildOutcomeService, buildRunnerService)

class BuildFailingBlock(
    notificationService: NotificationService,
    buildOutcomeService: BuildOutcomeService,
    buildRunnerService: BuildRunnerService
) : BaseBlock(notificationService, buildOutcomeService, buildRunnerService)

class RepositoryBlock
