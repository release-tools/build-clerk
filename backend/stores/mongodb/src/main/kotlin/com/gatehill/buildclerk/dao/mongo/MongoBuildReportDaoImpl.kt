package com.gatehill.buildclerk.dao.mongo

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.dao.mongo.model.MongoBuildDetails
import com.gatehill.buildclerk.dao.mongo.model.MongoBuildReport
import com.gatehill.buildclerk.dao.mongo.model.MongoScm
import com.gatehill.buildclerk.dao.mongo.model.toBuildReport
import com.gatehill.buildclerk.dao.mongo.model.toMongoBuildReport
import org.litote.kmongo.descending
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne

class MongoBuildReportDaoImpl : AbstractMongoDao(), BuildReportDao {
    override fun save(
        report: BuildReport
    ) = withCollection<MongoBuildReport, Unit> {
        insertOne(report.toMongoBuildReport())
    }

    override fun fetchLastBuildForBranch(
        branchName: String
    ): BuildReport? = withCollection<MongoBuildReport, BuildReport?> {
        find(MongoBuildReport::name eq branchName)
            .sort(descending(MongoBuildReport::createdDate))
            .limit(1).first()?.toBuildReport()
    }

    override fun hasEverSucceeded(
        commit: String
    ): Boolean = withCollection<MongoBuildReport, Boolean> {
        findOne(MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::commit eq commit) != null
    }

    override fun lastPassingCommitForBranch(
        branchName: String
    ): BuildReport? = withCollection<MongoBuildReport, BuildReport?> {
        find(
            MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::branch eq branchName,
            MongoBuildReport::build / MongoBuildDetails::status eq BuildStatus.SUCCESS
        ).run {
            sort(descending(MongoBuildReport::createdDate))
                .limit(1).first()
                ?.toBuildReport()
        }
    }

    override fun countStatusForCommitOnBranch(
        commit: String,
        branchName: String,
        status: BuildStatus
    ): Int = withCollection<MongoBuildReport, Int> {
        find(
            MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::branch eq branchName,
            MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::commit eq commit,
            MongoBuildReport::build / MongoBuildDetails::status eq status
        ).run {
            sort(descending(MongoBuildReport::createdDate)).count()
        }
    }

    override fun fetchBuildStatus(
        branchName: String,
        buildNumber: Int
    ): BuildStatus = withCollection<MongoBuildReport, BuildStatus> {
        find(
            MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::branch eq branchName,
            MongoBuildReport::build / MongoBuildDetails::number eq buildNumber
        ).run {
            sort(descending(MongoBuildReport::createdDate))
                .limit(1).first()?.build?.status
                ?: BuildStatus.UNKNOWN
        }
    }

    override fun countConsecutiveFailuresOnBranch(
        branchName: String
    ): Int = withCollection<MongoBuildReport, Int> {
        // Note: find() returns a cursor, so only the required results are fetched, until takeWhile {} terminates.
        // see: `KMongoIterable.takeWhile`
        find(MongoBuildReport::build / MongoBuildDetails::scm / MongoScm::branch eq branchName)
            .sort(descending(MongoBuildReport::createdDate))
            .takeWhile { it.build.status == BuildStatus.FAILED }
            .count()
    }
}
