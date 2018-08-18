package com.gatehill.buildclerk.dao.mongo

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.dao.mongo.config.MongoSettings
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
    override fun save(report: BuildReport) = withCollection {
        insertOne(report.toMongoBuildReport())
    }

    override fun fetchLastBuildForBranch(branchName: String): BuildReport? = withCollection {
        find(MongoBuildReport::name eq branchName)
                .sort(descending(MongoBuildReport::createdDate))
                .limit(1).first()?.toBuildReport()
    }

    override fun hasEverSucceeded(commit: String): Boolean = withCollection {
        findOne("{ 'build.scm.commit': '$commit' }") != null
    }

    override fun lastPassingCommitForBranch(branchName: String): BuildReport? = withCollection {
        find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.status': '${BuildStatus.SUCCESS}' } ] }")
                .sort(descending(MongoBuildReport::createdDate))
                .limit(1).first()?.toBuildReport()
    }

    override fun countFailuresForCommitOnBranch(commit: String, branchName: String): Int = withCollection {
        find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.scm.commit': '$commit' }, { 'build.status': '${BuildStatus.FAILED}' } ] }")
                .sort(descending(MongoBuildReport::createdDate))
                .count()
    }

    override fun fetchBuildStatus(branchName: String, buildNumber: Int): BuildStatus = withCollection {
        find("{ \$and: [ { 'build.scm.branch': '$branchName' }, { 'build.number': $buildNumber } ] }")
                .sort(descending(MongoBuildReport::createdDate))
                .limit(1).first()?.build?.status ?: BuildStatus.UNKNOWN
    }

    override fun countConsecutiveFailuresOnBranch(branchName: String): Int = withCollection {
        // find() returns a cursor, so only the required results are fetched, until takeWhile {} terminates.
        // see: `KMongoIterable.takeWhile`

        find("{ 'build.scm.branch': '$branchName' }")
                .sort(descending(MongoBuildReport::createdDate))
                .takeWhile { it.build.status == BuildStatus.FAILED }
                .count()
    }

    /**
     * Execute `block` on a Mongo collection and close the client after use.
     */
    private fun <T> withCollection(
            block: MongoCollection<MongoBuildReport>.() -> T
    ): T = KMongo.createClient(
            host = MongoSettings.host,
            port = MongoSettings.port
    ).use { client ->
        val database = client.getDatabase("clerk")
        val collection = database.getCollection<MongoBuildReport>("buildreport")
        collection.block()
    }
}
