package com.gatehill.buildclerk.dao.mongo.config

object MongoSettings {
    val host: String by lazy { 
        System.getenv("MONGO_HOST") ?: throw IllegalStateException("Missing MongoDB host")
    }
    val port: Int by lazy {
        System.getenv("MONGO_PORT")?.toInt() ?: throw IllegalStateException("Missing MongoDB port")
    }
}
