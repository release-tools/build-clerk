package io.gatehill.buildclerk.service.builder

import io.gatehill.buildclerk.api.dao.BuildReportDao
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.service.BuildReportService
import org.apache.logging.log4j.LogManager
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildReportServiceImpl @Inject constructor(
    private val buildReportDao: BuildReportDao
) : BuildReportService {
    private val logger = LogManager.getLogger(BuildReportServiceImpl::class.java)

    override val count
        get() = buildReportDao.count

    override val oldestDate
        get() = buildReportDao.oldestDate

    override val newestDate
        get() = buildReportDao.newestDate

    override fun record(buildReport: BuildReport) {
        logger.info("Saving report for branch: ${buildReport.build.scm.branch} with status: ${buildReport.build.status}")
        buildReportDao.save(buildReport)
    }

    override fun fetchLastReport(branchName: String?): BuildReport? =
        buildReportDao.fetchLast(branchName)

    override fun hasEverSucceeded(commit: String): Boolean =
        buildReportDao.hasEverSucceeded(commit)

    override fun fetchLastPassingBuildForBranch(branchName: String): BuildReport? =
        buildReportDao.lastPassingCommitForBranch(branchName)

    override fun countStatusForCommitOnBranch(commit: String, branch: String, status: BuildStatus): Int =
        buildReportDao.countStatusForCommitOnBranch(commit, branch, status)

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus =
        buildReportDao.fetchBuildStatus(branchName, buildNumber)

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int =
        buildReportDao.countConsecutiveFailuresOnBranch(branchName)

    override fun fetchReports(branchName: String?): List<BuildReport> =
        buildReportDao.list(branchName)

    override fun fetchReportsBetween(branchName: String?, start: ZonedDateTime, end: ZonedDateTime): List<BuildReport> =
            buildReportDao.fetchBetween(branchName, start, end)
}
