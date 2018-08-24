package com.gatehill.buildclerk.dao.mongo.model

import com.gatehill.buildclerk.api.model.BuildDetails
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.Scm
import java.time.ZonedDateTime

internal fun BuildReport.toMongoBuildReport() = MongoBuildReport(
    name = this.name,
    url = this.url,
    createdDate = ZonedDateTime.now(),
    build = MongoBuildDetails(
        number = this.build.number,
        fullUrl = this.build.fullUrl,
        status = this.build.status,
        triggeredBy = this.build.triggeredBy,
        scm = MongoScm(
            branch = this.build.scm.branch,
            commit = this.build.scm.commit
        )
    )
)

internal fun MongoBuildReport.toBuildReport() = BuildReport(
    name = this.name,
    url = this.url,
    build = BuildDetails(
        number = this.build.number,
        fullUrl = this.build.fullUrl,
        status = this.build.status,
        triggeredBy = this.build.triggeredBy,
        scm = Scm(
            branch = this.build.scm.branch,
            commit = this.build.scm.commit
        )
    )
)
