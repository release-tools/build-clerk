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
import io.gatehill.buildclerk.api.service.AnalysisService
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
    var pullRequestMerged: (PullRequestMergedBlock.() -> Unit)? = null
    var repository: (RepositoryBlock.() -> Unit)? = null
}

/**
 * Behaviour relating to a commit.
 */
interface CommitBlock {
    val commit: String

    fun revertCommit()
}

abstract class AbstractBaseBlock @Inject constructor(
    private val notificationService: NotificationService,
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService
    ) {
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
        buildReportService.fetchLastBuildForBranch(branchName)?.let { lastBuild ->
            val currentStatus = if (lastBuild.build.status == BuildStatus.SUCCESS) "healthy" else "unhealthy"
            val branchStatus = analysisService.analyseCommitHistory(
                branchName = branchName,
                commit = lastBuild.build.scm.commit
            )

            val color = when (lastBuild.build.status ) {
                BuildStatus.SUCCESS -> Color.GREEN
                else -> Color.RED
            }

            val summary = """
                Summary of `$branchName`:
                Current status is $currentStatus
                Most recent commit `${lastBuild.build.scm.commit}` has $branchStatus
            """.trimIndent()

            notificationService.notify(channelName, summary, color.hexCode)

        } ?: run {
            val summary = """
                Summary of `$branchName`:
                No history for branch.
            """.trimIndent()

            notificationService.notify(channelName, summary, Color.BLACK.hexCode)
        }
    }
}

abstract class AbstractBranchBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBaseBlock(
    notificationService,
    buildReportService,
    analysisService
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
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBranchBlock(
    notificationService,
    buildReportService,
    analysisService
), CommitBlock {

    lateinit var report: BuildReport

    override val commit: String
        get() = report.build.scm.commit

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

    override fun revertCommit() {
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
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBuildBlock(
    notificationService,
    buildReportService,
    analysisService
)

class BuildFailedBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBuildBlock(
    notificationService,
    buildReportService,
    analysisService
)

class BuildHealthyBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBuildBlock(
    notificationService,
    buildReportService,
    analysisService
)

class BuildFailingBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBuildBlock(
    notificationService,
    buildReportService,
    analysisService
)

class RepositoryBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBranchBlock(
    notificationService,
    buildReportService,
    analysisService
)

class PullRequestMergedBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService,
    private val pullRequestEventService: PullRequestEventService
) : AbstractBranchBlock(
    notificationService,
    buildReportService,
    analysisService
), CommitBlock {

    lateinit var mergeEvent: PullRequestMergedEvent
    lateinit var currentBranchStatus: BuildStatus

    override val commit: String
        get() = mergeEvent.pullRequest.mergeCommit.hash

    val prSummary: String
        get() = pullRequestEventService.describePullRequest(mergeEvent)

    override fun revertCommit() {
        analysis.recommend(
            RevertCommitAction(
                commit = mergeEvent.pullRequest.mergeCommit.hash,
                branch = branchName
            )
        )
    }
}

class CronBlock @Inject constructor(
    notificationService: NotificationService,
    buildReportService: BuildReportService,
    analysisService: AnalysisService
) : AbstractBaseBlock(
    notificationService,
    buildReportService,
    analysisService
)

/**
 * Entrypoint into the DSL.
 */
fun config(block: ConfigBlock.() -> Unit): ConfigBlock = ConfigBlock(block)
