package com.gatehill.buildclerk.service.builder

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.service.BuildReportService
import org.apache.logging.log4j.LogManager
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

    override fun record(buildReport: BuildReport) {
        logger.info("Saving report for branch: ${buildReport.build.scm.branch} with status: ${buildReport.build.status}")
        buildReportDao.save(buildReport)
    }

    override fun fetchLastBuildForBranch(branchName: String): BuildReport? =
        buildReportDao.fetchLastBuildForBranch(branchName)

    override fun hasEverSucceeded(commit: String): Boolean =
        buildReportDao.hasEverSucceeded(commit)

    override fun lastPassingBuildForBranch(branchName: String): BuildReport? =
        buildReportDao.lastPassingCommitForBranch(branchName)

    override fun countFailuresForCommitOnBranch(commit: String, branch: String): Int =
        buildReportDao.countFailuresForCommitOnBranch(commit, branch)

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus =
        buildReportDao.fetchBuildStatus(branchName, buildNumber)

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int =
        buildReportDao.countConsecutiveFailuresOnBranch(branchName)
}
