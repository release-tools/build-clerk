package com.gatehill.buildclerk.dsl

import com.gatehill.buildclerk.api.model.Analysis
import com.gatehill.buildclerk.api.model.BuildOutcome
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.api.model.action.LockBranchAction
import com.gatehill.buildclerk.api.model.action.RebuildBranchAction
import com.gatehill.buildclerk.api.model.action.RevertAction
import com.gatehill.buildclerk.api.service.BuildOutcomeService
import com.gatehill.buildclerk.api.service.NotificationService
import javax.inject.Inject

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

    fun pullRequestMerged(body: PullRequestMergedBlock.() -> Unit) {
        bodyHolder.pullRequestMerged = body
    }
}

class BodyHolder {
    var buildPassed: (BuildPassedBlock.() -> Unit)? = null
    var buildFailed: (BuildFailedBlock.() -> Unit)? = null
    var branchStartsPassing: (BuildHealthyBlock.() -> Unit)? = null
    var branchStartsFailing: (BuildFailingBlock.() -> Unit)? = null
    var pullRequestMerged: (PullRequestMergedBlock.() -> Unit)? = null
    var repository: (RepositoryBlock.() -> Unit)? = null
}

abstract class AbstractBlock @Inject constructor(
        private val notificationService: NotificationService,
        private val buildOutcomeService: BuildOutcomeService
) {
    lateinit var analysis: Analysis
    lateinit var branchName: String

    fun log(message: String) = analysis.log(message)

    val consecutiveFailuresOnBranch: Int by lazy {
        buildOutcomeService.countConsecutiveFailuresOnBranch(branchName)
    }

    val lastPassingCommitForBranch: BuildOutcome? by lazy {
        buildOutcomeService.lastPassingCommitForBranch(branchName)
    }

    fun lockBranch() {
        analysis.recommend(
                LockBranchAction(branchName)
        )
    }

    fun notifyChannel(channelName: String, message: String, color: Colour = Colour.BLACK) =
            notifyChannel(channelName, message, color.hexCode)

    fun notifyChannel(channelName: String, message: String, color: String = "#000000") =
            notificationService.notify(channelName, message, color)

    fun postAnalysisToChannel(channelName: String, color: Colour = Colour.BLACK) =
            postAnalysisToChannel(channelName, color.hexCode)

    fun postAnalysisToChannel(channelName: String, color: String = "#000000") =
            notificationService.notify(channelName, analysis, color)
}

abstract class AbstractBuildBlock @Inject constructor(
        notificationService: NotificationService,
        private val buildOutcomeService: BuildOutcomeService
) : AbstractBlock(
        notificationService,
        buildOutcomeService
) {

    lateinit var outcome: BuildOutcome

    val commitHasEverSucceeded: Boolean by lazy {
        buildOutcomeService.hasEverSucceeded(outcome.build.scm.commit)
    }

    val failuresForCommitOnBranch: Int by lazy {
        buildOutcomeService.countFailuresForCommitOnBranch(outcome.build.scm.commit, branchName)
    }

    fun rebuildBranch() {
        analysis.recommend(
                RebuildBranchAction(outcome)
        )
    }

    fun revertCommit() {
        analysis.recommend(
                RevertAction(
                        commit = outcome.build.scm.commit,
                        branch = branchName
                )
        )
    }
}

class BuildPassedBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBuildBlock(
        notificationService,
        buildOutcomeService
)

class BuildFailedBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBuildBlock(
        notificationService,
        buildOutcomeService
)

class BuildHealthyBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBuildBlock(
        notificationService,
        buildOutcomeService
)

class BuildFailingBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBuildBlock(
        notificationService,
        buildOutcomeService
)

class RepositoryBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBlock(
        notificationService,
        buildOutcomeService
)

class PullRequestMergedBlock @Inject constructor(
        notificationService: NotificationService,
        buildOutcomeService: BuildOutcomeService
) : AbstractBlock(
        notificationService,
        buildOutcomeService
) {
    lateinit var mergeEvent: PullRequestMergedEvent
    lateinit var currentBranchStatus: BuildStatus

    val prSummary: String
        get() = "#${mergeEvent.pullRequest.id} [author: ${mergeEvent.pullRequest.author.username}]"

    fun revertCommit() {
        analysis.recommend(
                RevertAction(
                        commit = mergeEvent.pullRequest.mergeCommit.hash,
                        branch = branchName
                )
        )
    }
}

class Colour(val hexCode: String) {
    companion object {
        fun of(hexCode: String) = Colour(hexCode)
        val BLACK = of("#000000")
        val RED = of("#ff0000")
        val GREEN = of("#00ff00")
    }
}

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
