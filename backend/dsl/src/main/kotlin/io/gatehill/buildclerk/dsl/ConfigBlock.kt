package io.gatehill.buildclerk.dsl

import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.pr.PullRequestEvent
import io.gatehill.buildclerk.api.model.action.LockBranchAction
import io.gatehill.buildclerk.api.model.action.PendingAction
import io.gatehill.buildclerk.api.model.action.PendingActionSet
import io.gatehill.buildclerk.api.model.action.RebuildBranchAction
import io.gatehill.buildclerk.api.model.action.RevertCommitAction
import io.gatehill.buildclerk.api.model.action.ShowTextAction
import io.gatehill.buildclerk.api.model.analysis.Analysis
import io.gatehill.buildclerk.api.model.analysis.PublishConfig
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.SourceFile
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.BuildSummaryService
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
    val scheduledTasks = mutableListOf<Pair<String, CronBlock.() -> Unit>>()

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

    fun pullRequestModified(body: PullRequestModifiedBlock.() -> Unit) {
        bodyHolder.pullRequestModified = body
    }

    fun pullRequestMerged(body: PullRequestMergedBlock.() -> Unit) {
        bodyHolder.pullRequestMerged = body
    }

    /**
     * @param schedule in cron format: `seconds minutes hours day_of_month month day_of_year year`
     */
    fun cron(schedule: String, body: CronBlock.() -> Unit) {
        scheduledTasks += schedule to body
    }
}

class BodyHolder {
    var buildPassed: (BuildPassedBlock.() -> Unit)? = null
    var buildFailed: (BuildFailedBlock.() -> Unit)? = null
    var branchStartsPassing: (BuildHealthyBlock.() -> Unit)? = null
    var branchStartsFailing: (BuildFailingBlock.() -> Unit)? = null
    var pullRequestModified: (PullRequestModifiedBlock.() -> Unit)? = null
    var pullRequestMerged: (PullRequestMergedBlock.() -> Unit)? = null
    var repository: (RepositoryBlock.() -> Unit)? = null
}

interface BaseBlock {
    val notificationService: NotificationService
    val buildSummaryService: BuildSummaryService

    fun postMessage(
        channelName: String,
        message: String,
        color: Color = Color.BLACK
    ) = notificationService.notify(
        channelName,
        message,
        color.hexCode
    )

    fun publishSummary(
        channelName: String,
        branchName: String
    ) {
        val summary = buildSummaryService.summarise(branchName)
        notificationService.notify(channelName, summary.message, summary.color.hexCode)
    }
}

/**
 * Behaviour relating to an analysis.
 */
interface AnalysisBlock {
    val analysis: Analysis

    val perform: ActionCaptureBlock
        get() = ActionCaptureBlock(analysis.perform)

    val suggest: ActionCaptureBlock
        get() = ActionCaptureBlock(analysis.suggested)

    fun log(message: String) = analysis.log(message)

    fun showText(
        body: String,
        title: String = "Show",
        description: String? = null,
        color: Color = Color.BLACK,
        channelName: String? = null
    ) = ShowTextAction(
        body = body,
        title = title,
        description = description,
        color = color,
        channelName = channelName
    )

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

data class ActionCaptureBlock(
    private val actionSet: PendingActionSet
) {
    infix fun action(pendingAction: PendingAction) {
        if (!actionSet.actions.contains(pendingAction)) {
            actionSet.actions += pendingAction
        }
    }
}

/**
 * Behaviour relating to a commit.
 */
interface CommitBlock : AnalysisBlock {
    val buildReportService: BuildReportService
    val commit: String
    val branchName: String

    val commitHasEverSucceeded: Boolean
        get() = buildReportService.hasEverSucceeded(commit)

    val successesForCommitOnBranch: Int
        get() = buildReportService.countStatusForCommitOnBranch(commit, branchName, BuildStatus.SUCCESS)

    val failuresForCommitOnBranch: Int
        get() = buildReportService.countStatusForCommitOnBranch(commit, branchName, BuildStatus.FAILED)

    fun revertCommit() = RevertCommitAction(
        commit = commit,
        branch = branchName
    )
}

/**
 * Behaviour relating to a branch.
 */
interface BranchBlock : AnalysisBlock {
    val buildReportService: BuildReportService

    val branchName: String

    val consecutiveFailuresOnBranch: Int
        get() = buildReportService.countConsecutiveFailuresOnBranch(branchName)

    val lastPassingBuildForBranch: BuildReport?
        get() = buildReportService.fetchLastPassingBuildForBranch(branchName)

    val lastPassingCommitForBranch: String?
        get() = lastPassingBuildForBranch?.build?.scm?.commit

    fun lockBranch() = LockBranchAction(branchName)
}

/**
 * Behaviour for build reports.
 */
abstract class BuildBlock : AnalysisBlock, BranchBlock, CommitBlock, BaseBlock {
    override lateinit var analysis: Analysis
    lateinit var report: BuildReport

    override val branchName: String
        get() = report.build.scm.branch

    override val commit: String
        get() = report.build.scm.commit

    fun rebuildBranch() = RebuildBranchAction(report)
}

class BuildPassedBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BuildBlock()

class BuildFailedBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BuildBlock()

class BuildHealthyBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BuildBlock()

class BuildFailingBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BuildBlock()

class RepositoryBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BuildBlock()

interface PullRequestBlock {
    val pullRequestEventService: PullRequestEventService
    val pullRequestEvent: PullRequestEvent

    val prSummary: String
        get() = pullRequestEventService.describePullRequest(pullRequestEvent)

    fun ensureComment(comment: String) {
        pullRequestEventService.ensureComment(pullRequestEvent.pullRequest.id, comment)
    }
}

class PullRequestMergedBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService,
    override val pullRequestEventService: PullRequestEventService
) : AnalysisBlock, CommitBlock, PullRequestBlock, BuildBlock() {

    override lateinit var pullRequestEvent: PullRequestMergedEvent
    lateinit var currentBranchStatus: BuildStatus

    override val branchName: String
        get() = pullRequestEvent.pullRequest.destination.branch.name

    override val commit: String
        get() = pullRequestEvent.pullRequest.mergeCommit.hash
}

class PullRequestModifiedBlock @Inject constructor(
    override val buildReportService: BuildReportService,
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService,
    override val pullRequestEventService: PullRequestEventService
) : AnalysisBlock, PullRequestBlock, BuildBlock() {

    override lateinit var pullRequestEvent: PullRequestEvent

    lateinit var files: List<SourceFile>
}

class CronBlock @Inject constructor(
    override val notificationService: NotificationService,
    override val buildSummaryService: BuildSummaryService
) : BaseBlock

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
