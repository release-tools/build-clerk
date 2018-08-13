package com.gatehill.buildclerk.service.builder

import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.api.service.BuildReportService
import com.gatehill.buildclerk.api.dao.BuildReportDao
import org.apache.logging.log4j.LogManager

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BuildReportServiceImpl(
        private val buildReportDao: BuildReportDao
) : BuildReportService {

    private val logger = LogManager.getLogger(BuildReportServiceImpl::class.java)

    private val store : MutableList<BuildReport>
        get() = buildReportDao.readStore()

    override fun updateStatus(buildReport: BuildReport) {
        logger.info("Updating status for branch: ${buildReport.build.scm.branch} to: ${buildReport.build.status}")
        store += buildReport
    }

    override fun fetchStatus(branchName: String): BuildReport? = store.asReversed().firstOrNull { outcome ->
        outcome.build.scm.branch == branchName
    }

    override fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any { outcome ->
        outcome.build.scm.commit == commit && outcome.build.status == BuildStatus.SUCCESS
    }

    override fun lastPassingCommitForBranch(branchName: String): BuildReport? = store.asReversed().find { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.status == BuildStatus.SUCCESS
    }

    override fun countFailuresForCommitOnBranch(commit: String, branch: String): Int =
            store.asReversed().count { outcome ->
                outcome.build.scm.commit == commit && outcome.build.scm.branch == branch && outcome.build.status == BuildStatus.FAILED
            }

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus? = store.lastOrNull { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.number == buildNumber
    }?.build?.status

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int = store.takeLastWhile { outcome ->
        outcome.build.scm.branch == branchName && outcome.build.status == BuildStatus.FAILED
    }.size
}
