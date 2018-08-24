package com.gatehill.buildclerk.dao.mongo.model

import com.gatehill.buildclerk.api.model.BuildStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.ZonedDateTime

data class MongoBuildReport(
    val name: String,
    val url: String,
    val build: MongoBuildDetails,

    /**
     * Just for persistence.
     */
    val createdDate: ZonedDateTime
) {
    @BsonId
    val key: Id<MongoBuildReport> = newId()
}

data class MongoBuildDetails(
    val number: Int,
    val status: BuildStatus,
    val scm: MongoScm,
    val fullUrl: String,
    val triggeredBy: String? = null
) {
    @BsonId
    val key: Id<MongoBuildDetails> = newId()
}

data class MongoScm(
    val branch: String,
    val commit: String
) {
    @BsonId
    val key: Id<MongoScm> = newId()
}
