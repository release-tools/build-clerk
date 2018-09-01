package io.gatehill.buildclerk.service

import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.BuildSummary
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.BuildSummaryService
import io.gatehill.buildclerk.api.util.Color
import javax.inject.Inject

class BuildSummaryServiceImpl @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService
) : BuildSummaryService {

    override fun summarise(branchName: String): BuildSummary {
        return buildReportService.fetchLastReport(branchName)?.let { lastBuild ->
            val currentStatus = if (lastBuild.build.status == BuildStatus.SUCCESS) "healthy" else "unhealthy"
            val branchStatus = analysisService.analyseCommitHistory(
                branchName = branchName,
                commit = lastBuild.build.scm.commit
            )

            val color = when (lastBuild.build.status) {
                BuildStatus.SUCCESS -> Color.GREEN
                else -> Color.RED
            }

            val summary = """
                Summary of `$branchName`:
                Current status is $currentStatus
                Most recent commit `${lastBuild.build.scm.commit}` has $branchStatus
            """.trimIndent()

            BuildSummary(summary, color)

        } ?: run {
            val summary = """
                Summary of `$branchName`:
                No history for branch.
            """.trimIndent()

            BuildSummary(summary, Color.BLACK)
        }
    }
}
