package com.gatehill.buildclerk.dao.mongo

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.dao.mongo.model.MongoBuildReport
import com.gatehill.buildclerk.dao.mongo.model.toBuildReport
import com.gatehill.buildclerk.dao.mongo.model.toMongoBuildReport
import com.mongodb.client.MongoCollection
import org.litote.kmongo.KMongo
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoBuildReportDaoImpl : BuildReportDao {
    private val collection: MongoCollection<MongoBuildReport>
        get() {
            val client = KMongo.createClient()
            val database = client.getDatabase("clerk")
            return database.getCollection<MongoBuildReport>("buildreport")
        }

    override fun save(report: BuildReport) {
        collection.insertOne(report.toMongoBuildReport())
    }

    override fun fetchLastBuildForBranch(branchName: String): BuildReport? = collection
            .find(MongoBuildReport::name eq branchName)
            .sort(descending(MongoBuildReport::createdDate))
            .limit(1).first()?.toBuildReport()

    override fun hasEverSucceeded(commit: String): Boolean = collection
            .findOne("{ 'build.scm.commit': '$commit' }") != null

    override fun lastPassingCommitForBranch(branchName: String): BuildReport? = collection
            .find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.status': '${BuildStatus.SUCCESS}' } ] }")
            .sort(descending(MongoBuildReport::createdDate))
            .limit(1).first()?.toBuildReport()

    override fun countFailuresForCommitOnBranch(commit: String, branchName: String): Int = collection
            .find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.scm.commit': '$commit' }, { 'build.status': '${BuildStatus.FAILED}' } ] }")
            .sort(descending(MongoBuildReport::createdDate))
            .count()

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus = collection
            .find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.number': $buildNumber } ] }")
            .sort(descending(MongoBuildReport::createdDate))
            .limit(1).first()?.build?.status ?: BuildStatus.UNKNOWN

    /**
     * This is not very efficient as the filtering happens in memory, after
     * retrieving all reports for a branch.
     */
    override fun countConsecutiveFailuresOnBranch(branchName: String): Int {
        val branchFailures = collection
                .find("{ 'build.scm.branch': '$branchName' }")
                .sort(descending(MongoBuildReport::createdDate))

        return branchFailures.takeWhile { it.build.status == BuildStatus.FAILED }.count()
    }
}
