package com.gatehill.buildclerk.dsl

import com.gatehill.buildclerk.api.model.Analysis
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.api.model.action.LockBranchAction
import com.gatehill.buildclerk.api.model.action.RebuildBranchAction
import com.gatehill.buildclerk.api.model.action.RevertAction
import com.gatehill.buildclerk.api.service.BuildReportService
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
        private val buildReportService: BuildReportService
) {
    lateinit var analysis: Analysis
    lateinit var branchName: String

    fun log(message: String) = analysis.log(message)

    val consecutiveFailuresOnBranch: Int by lazy {
        buildReportService.countConsecutiveFailuresOnBranch(branchName)
    }

    val lastPassingCommitForBranch: BuildReport? by lazy {
        buildReportService.lastPassingCommitForBranch(branchName)
    }

    fun lockBranch() {
        analysis.recommend(
                LockBranchAction(branchName)
        )
    }

    fun notifyChannel(channelName: String, message: String, color: Color = Color.BLACK) =
            notificationService.notify(channelName, message, color.hexCode)

    fun postAnalysisToChannel(channelName: String, color: Color = Color.BLACK) =
            notificationService.notify(channelName, analysis, color.hexCode)

}

abstract class AbstractBuildBlock @Inject constructor(
        notificationService: NotificationService,
        private val buildReportService: BuildReportService
) : AbstractBlock(
        notificationService,
        buildReportService
) {

    lateinit var report: BuildReport

    val commitHasEverSucceeded: Boolean by lazy {
        buildReportService.hasEverSucceeded(report.build.scm.commit)
    }

    val failuresForCommitOnBranch: Int by lazy {
        buildReportService.countFailuresForCommitOnBranch(report.build.scm.commit, branchName)
    }

    fun rebuildBranch() {
        analysis.recommend(
                RebuildBranchAction(report)
        )
    }

    fun revertCommit() {
        analysis.recommend(
                RevertAction(
                        commit = report.build.scm.commit,
                        branch = branchName
                )
        )
    }
}

class BuildPassedBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBuildBlock(
        notificationService,
        buildReportService
)

class BuildFailedBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBuildBlock(
        notificationService,
        buildReportService
)

class BuildHealthyBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBuildBlock(
        notificationService,
        buildReportService
)

class BuildFailingBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBuildBlock(
        notificationService,
        buildReportService
)

class RepositoryBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBlock(
        notificationService,
        buildReportService
)

class PullRequestMergedBlock @Inject constructor(
        notificationService: NotificationService,
        buildReportService: BuildReportService
) : AbstractBlock(
        notificationService,
        buildReportService
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

class Color(val hexCode: String) {
    companion object {
        fun of(hexCode: String) = Color(hexCode)
        val BLACK = of("#000000")
        val RED = of("#ff0000")
        val GREEN = of("#00ff00")
    }
}

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
