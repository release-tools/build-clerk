package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.Recorded
import io.gatehill.buildclerk.dao.mongo.model.Dated
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
import java.time.ZonedDateTime

abstract class AbstractRecordedMongoDao : AbstractMongoDao(), Recorded {
    protected inline fun <reified T : Any> count() = withCollection<T, Int> {
        countDocuments().toInt()
    }

    protected inline fun <reified T : Dated> oldestDate() = withCollection<T, ZonedDateTime?> {
        find()
            .sort(ascending(Dated::createdDate))
            .limit(1)
            .firstOrNull()
            ?.createdDate
    }

    protected inline fun <reified T : Dated> newestDate() = withCollection<T, ZonedDateTime?> {
        find()
            .sort(descending(Dated::createdDate))
            .limit(1)
            .firstOrNull()
            ?.createdDate
    }
}
