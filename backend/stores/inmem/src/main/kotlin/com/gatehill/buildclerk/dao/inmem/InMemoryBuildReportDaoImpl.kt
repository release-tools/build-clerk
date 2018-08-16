package com.gatehill.buildclerk.dao.inmem

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus

class InMemoryBuildReportDaoImpl : BuildReportDao {
    private val store = mutableListOf<BuildReport>()

    override fun save(report: BuildReport) {
        store += report
    }

    override fun fetchLastBuildForBranch(branchName: String): BuildReport? = store.asReversed().firstOrNull { report ->
        report.build.scm.branch == branchName
    }

    override fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any { report ->
        report.build.scm.commit == commit && report.build.status == BuildStatus.SUCCESS
    }

    override fun lastPassingCommitForBranch(branchName: String): BuildReport? = store.asReversed().find { report ->
        report.build.scm.branch == branchName && report.build.status == BuildStatus.SUCCESS
    }

    override fun countFailuresForCommitOnBranch(commit: String, branch: String): Int =
            store.asReversed().count { report ->
                report.build.scm.commit == commit && report.build.scm.branch == branch && report.build.status == BuildStatus.FAILED
            }

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus = store.lastOrNull { report ->
        report.build.scm.branch == branchName && report.build.number == buildNumber
    }?.build?.status ?: BuildStatus.UNKNOWN

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int = store.takeLastWhile { report ->
        report.build.scm.branch == branchName && report.build.status == BuildStatus.FAILED
    }.size
}
