package io.gatehill.buildclerk.dsl

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.action.LockBranchAction
import io.gatehill.buildclerk.api.model.action.RebuildBranchAction
import io.gatehill.buildclerk.api.model.action.RevertCommitAction
import io.gatehill.buildclerk.api.model.action.ShowTextAction
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.model.analysis.PublishConfig
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.api.util.Color
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

    val lastPassingBuildForBranch: BuildReport? by lazy {
        buildReportService.lastPassingBuildForBranch(branchName)
    }

    val lastPassingCommitForBranch: String? by lazy {
        lastPassingBuildForBranch?.build?.scm?.commit
    }

    fun showText(
        body: String,
        title: String = "Show",
        description: String? = null,
        color: Color = Color.BLACK,
        channelName: String? = null
    ) {
        analysis.recommend(
            ShowTextAction(
                body = body,
                title = title,
                description = description,
                color = color,
                channelName = channelName
            )
        )
    }

    fun lockBranch() {
        analysis.recommend(LockBranchAction(branchName))
    }

    fun postMessage(
        channelName: String,
        message: String,
        color: Color = Color.BLACK
    ) =
        notificationService.notify(channelName, message, color.hexCode)

    fun publishAnalysis(
        channelName: String,
        color: Color = Color.BLACK
    ) {
        analysis.publishConfig = PublishConfig(
            channelName = channelName,
            color = color
        )
    }
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

    val successesForCommitOnBranch: Int by lazy {
        buildReportService.countStatusForCommitOnBranch(report.build.scm.commit, branchName, BuildStatus.SUCCESS)
    }

    val failuresForCommitOnBranch: Int by lazy {
        buildReportService.countStatusForCommitOnBranch(report.build.scm.commit, branchName, BuildStatus.FAILED)
    }

    fun rebuildBranch() {
        analysis.recommend(RebuildBranchAction(report))
    }

    fun revertCommit() {
        analysis.recommend(
            RevertCommitAction(
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
    buildReportService: BuildReportService,
    private val pullRequestEventService: PullRequestEventService
) : AbstractBlock(
    notificationService,
    buildReportService
) {
    lateinit var mergeEvent: PullRequestMergedEvent
    lateinit var currentBranchStatus: BuildStatus

    val prSummary: String
        get() = pullRequestEventService.describePullRequest(mergeEvent)

    fun revertCommit() {
        analysis.recommend(
            RevertCommitAction(
                commit = mergeEvent.pullRequest.mergeCommit.hash,
                branch = branchName
            )
        )
    }
}

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
