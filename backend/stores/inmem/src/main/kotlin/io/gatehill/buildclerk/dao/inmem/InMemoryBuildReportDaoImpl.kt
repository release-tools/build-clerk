package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.BuildReportDao
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.dao.inmem.model.Record

class InMemoryBuildReportDaoImpl : AbstractInMemoryDao<BuildReport>(), BuildReportDao {
    override val store = mutableListOf<Record<BuildReport>>()

    override fun save(report: BuildReport) {
        store += Record.create(report)
    }

    override fun fetchLast(branchName: String?): BuildReport? = store.asReversed().firstOrNull { wrapper ->
        branchName?.let { wrapper.record.build.scm.branch == branchName } ?: true
    }?.record

    override fun hasEverSucceeded(commit: String): Boolean = store.asReversed().any {
        it.record.build.scm.commit == commit && it.record.build.status == BuildStatus.SUCCESS
    }

    override fun lastPassingCommitForBranch(branchName: String): BuildReport? = store.asReversed().find {
        it.record.build.scm.branch == branchName && it.record.build.status == BuildStatus.SUCCESS
    }?.record

    override fun countStatusForCommitOnBranch(commit: String, branchName: String, status: BuildStatus): Int =
        store.count {
            it.record.build.scm.commit == commit && it.record.build.scm.branch == branchName && it.record.build.status == status
        }

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus = store.lastOrNull {
        it.record.build.scm.branch == branchName && it.record.build.number == buildNumber
    }?.record?.build?.status ?: BuildStatus.UNKNOWN

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int = store.takeLastWhile {
        it.record.build.scm.branch == branchName && it.record.build.status == BuildStatus.FAILED
    }.size

    override fun list(branchName: String?): List<BuildReport> = store.filter { wrapper ->
        branchName?.let { wrapper.record.build.scm.branch == branchName } ?: true
    }.map {
        it.record
    }
}
