package io.gatehill.buildclerk.dao.mongo

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import io.gatehill.buildclerk.dao.mongo.config.MongoSettings
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

abstract class AbstractMongoDao {
    abstract val collectionName: String

    /**
     * Execute `block` on a Mongo collection and close the client after use.
     */
    protected inline fun <reified T : Any, R> withCollection(
        block: MongoCollection<T>.() -> R
    ): R = KMongo.createClient(
        addr = ServerAddress(MongoSettings.host, MongoSettings.port),
        credentialsList = buildMongoCredentials()
    ).use { client ->
        val database = client.getDatabase(databaseName)
        val collection = database.getCollection<T>(collectionName)
        collection.block()
    }

    companion object {
        protected const val databaseName = "clerk"

        protected fun buildMongoCredentials(): List<MongoCredential> = MongoSettings.userName?.let { userName ->
            listOf(
                MongoCredential.createCredential(
                    userName,
                    databaseName,
                    MongoSettings.password.toCharArray()
                )
            )
        } ?: emptyList()
    }
}
