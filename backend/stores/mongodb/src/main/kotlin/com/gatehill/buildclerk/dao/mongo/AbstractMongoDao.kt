package com.gatehill.buildclerk.dao.mongo

import com.gatehill.buildclerk.dao.mongo.config.MongoSettings
import com.mongodb.client.MongoCollection
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

abstract class AbstractMongoDao {
    /**
     * Execute `block` on a Mongo collection and close the client after use.
     */
    protected inline fun <reified T : Any, R> withCollection(
        block: MongoCollection<T>.() -> R
    ): R = KMongo.createClient(
        host = MongoSettings.host,
        port = MongoSettings.port
    ).use { client ->
        val database = client.getDatabase("clerk")
        val collection = database.getCollection<T>()
        collection.block()
    }
}
