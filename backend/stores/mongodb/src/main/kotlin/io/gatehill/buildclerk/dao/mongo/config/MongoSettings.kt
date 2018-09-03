package io.gatehill.buildclerk.dao.mongo.config

object MongoSettings {
    val host: String by lazy {
        System.getenv("MONGO_HOST") ?: "localhost"
    }
    val port: Int by lazy {
        System.getenv("MONGO_PORT")?.toInt() ?: 27017
    }
    val userName: String? by lazy {
        System.getenv("MONGO_USERNAME")
    }
    val password: String by lazy {
        System.getenv("MONGO_PASSWORD")
    }
}
