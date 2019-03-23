package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.BuildReportDao
import io.gatehill.buildclerk.api.model.BuildDetails
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.Scm
import io.gatehill.buildclerk.dao.mongo.model.MongoBuildReportWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.bson.BsonDocument
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.gt
import org.litote.kmongo.gte
import org.litote.kmongo.lt
import java.time.ZonedDateTime

class MongoBuildReportDaoImpl : AbstractRecordedMongoDao(), BuildReportDao {
    override val collectionName = "build_reports"

    override fun save(
        report: BuildReport
    ) = withCollection<MongoBuildReportWrapper, Unit> {
        insertOne(report.wrap())
    }

    override fun fetchLast(
        branchName: String?
    ): BuildReport? = withCollection<MongoBuildReportWrapper, BuildReport?> {

        val iterable = branchName?.let { find(filterByBranchName(branchName)) } ?: find()
        return@withCollection iterable
            .sort(descending(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number))
            .limit(1).first()?.buildReport
    }

    override fun hasEverSucceeded(
        commit: String
    ): Boolean = withCollection<MongoBuildReportWrapper, Boolean> {
        null != findOne(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::scm / Scm::commit eq commit)
    }

    override fun lastPassingCommitForBranch(
        branchName: String
    ): BuildReport? = withCollection<MongoBuildReportWrapper, BuildReport?> {
        find(
            filterByBranchName(branchName),
            MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::status eq BuildStatus.SUCCESS
        ).run {
            sort(descending(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number))
                .limit(1).first()
                ?.buildReport
        }
    }

    override fun countStatusForCommitOnBranch(
        commit: String,
        branchName: String,
        status: BuildStatus
    ): Int = withCollection<MongoBuildReportWrapper, Int> {
        find(
            filterByBranchName(branchName),
            MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::scm / Scm::commit eq commit,
            MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::status eq status
        ).count()
    }

    override fun fetchBuildStatus(
        branchName: String,
        buildNumber: Int
    ): BuildStatus = withCollection<MongoBuildReportWrapper, BuildStatus> {

        findOne(
            filterByBranchName(branchName),
            MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number eq buildNumber
        )?.buildReport?.build?.status
            ?: BuildStatus.UNKNOWN
    }

    override fun countConsecutiveFailuresOnBranch(
        branchName: String
    ): Int = withCollection<MongoBuildReportWrapper, Int> {
        // Note: find() returns a cursor, so only the required results are fetched, until takeWhile {} terminates.
        // see: `KMongoIterable.takeWhile`
        find(filterByBranchName(branchName))
            .sort(descending(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number))
            .takeWhile { it.buildReport.build.status == BuildStatus.FAILED }
            .count()
    }

    private fun filterByBranchName(branchName: String) =
        MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::scm / Scm::branch eq branchName

    override fun list(
        branchName: String?
    ): List<BuildReport> = withCollection<MongoBuildReportWrapper, List<BuildReport>> {

        val iterable = branchName?.let { find(filterByBranchName(branchName)) } ?: find()

        // convert to list to avoid leaking mongo connection when method returns
        return@withCollection iterable
            .sort(ascending(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number))
            .map { it.buildReport }
            .toList()
    }

    override fun fetchBetween(
        branchName: String?,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): List<BuildReport> = withCollection<MongoBuildReportWrapper, List<BuildReport>> {

        val branchFilter = branchName?.let { filterByBranchName(branchName) } ?: BsonDocument()

        val iterable = find(
            branchFilter,
            MongoBuildReportWrapper::createdDate gte start,
            MongoBuildReportWrapper::createdDate lt end
        )

        // convert to list to avoid leaking mongo connection when method returns
        return@withCollection iterable
            .sort(ascending(MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number))
            .map { it.buildReport }
            .toList()
    }

    override fun findHigherBuild(
        branchName: String,
        buildNumber: Int
    ): BuildReport? = withCollection<MongoBuildReportWrapper, BuildReport?> {

        findOne(
            filterByBranchName(branchName),
            MongoBuildReportWrapper::buildReport / BuildReport::build / BuildDetails::number gt buildNumber
        )?.buildReport
    }

    override val count
        get () = count<MongoBuildReportWrapper>()

    override val oldestDate: ZonedDateTime?
        get() = oldestDate<MongoBuildReportWrapper>()

    override val newestDate: ZonedDateTime?
        get() = newestDate<MongoBuildReportWrapper>()
}
