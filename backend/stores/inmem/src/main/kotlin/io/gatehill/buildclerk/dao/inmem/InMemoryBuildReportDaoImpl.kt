package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.BuildReportDao
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus

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

    override fun countStatusForCommitOnBranch(commit: String, branchName: String, status: BuildStatus): Int =
        store.count { report ->
            report.build.scm.commit == commit && report.build.scm.branch == branchName && report.build.status == status
        }

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus = store.lastOrNull { report ->
        report.build.scm.branch == branchName && report.build.number == buildNumber
    }?.build?.status ?: BuildStatus.UNKNOWN

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int = store.takeLastWhile { report ->
        report.build.scm.branch == branchName && report.build.status == BuildStatus.FAILED
    }.size

    override fun count() = store.size
}
