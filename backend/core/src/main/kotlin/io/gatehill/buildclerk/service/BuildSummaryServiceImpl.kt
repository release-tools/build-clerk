package io.gatehill.buildclerk.service

import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.BuildSummary
import io.gatehill.buildclerk.api.model.ReportSpan
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.BuildSummaryService
import io.gatehill.buildclerk.api.util.Color
import io.gatehill.buildclerk.api.util.toShortCommit
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.runBlocking
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject

class BuildSummaryServiceImpl @Inject constructor(
    private val buildReportService: BuildReportService,
    private val analysisService: AnalysisService
) : BuildSummaryService {

    private val startOfDay: ZonedDateTime
        get() = ZonedDateTime.now().with(LocalTime.MIN)

    override fun summarise(branchName: String): BuildSummary {
        return buildReportService.fetchLastReport(branchName)?.let { lastBuild ->

            runBlocking {
                val performBasicAnalysis = async {
                    analysisService.performBasicBuildAnalysis(lastBuild)
                }
                val analyseSpan = async {
                    analysisService.analyseReportSpan(branchName, startOfDay, ZonedDateTime.now())
                }

                awaitAll(performBasicAnalysis, analyseSpan)

                val basicAnalysis = performBasicAnalysis.getCompleted()
                val span = analyseSpan.getCompleted()
                val currentStatus = if (lastBuild.build.status == BuildStatus.SUCCESS) "healthy" else "unhealthy"

                val color = when (lastBuild.build.status) {
                    BuildStatus.SUCCESS -> Color.GREEN
                    else -> Color.RED
                }

                // no padding, to accommodate multiline basic analysis
                val summary = """
Summary of `$branchName` branch:
Current status is $currentStatus
Pass rate today is ${roundPassRateForDisplay(span)}% from ${span.dataPoints} builds
Most recent commit is `${toShortCommit(lastBuild.build.scm.commit)}`
${basicAnalysis.joinToString("\n")}
""".trimIndent()

                BuildSummary(summary, color)
            }

        } ?: run {
            val summary = """
Summary of `$branchName` branch:
No build history for branch.
""".trimIndent()

            BuildSummary(summary, Color.BLACK)
        }
    }

    /**
     * Round up the pass rate and multiply by 100, for display purposes.
     */
    private fun roundPassRateForDisplay(span: ReportSpan): String =
        DecimalFormat("#.#").format(span.passRate * 100)
}
