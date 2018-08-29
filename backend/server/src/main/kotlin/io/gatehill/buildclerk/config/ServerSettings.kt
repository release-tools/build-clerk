package io.gatehill.buildclerk.config

object ServerSettings {
    object Http {
        val port: Int by lazy { System.getenv("SERVER_PORT")?.toInt() ?: 9090 }
    }

    object Auth {
        /**
         * See https://vertx.io/docs/vertx-auth-shiro/kotlin/
         */
        val configFile: String? by lazy {
            System.getenv("AUTH_CONFIG_FILE")?.takeUnless(String::isEmpty)?.let { "file:$it" }
        }
    }
}
