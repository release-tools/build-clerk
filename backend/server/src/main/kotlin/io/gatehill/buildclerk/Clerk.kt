package io.gatehill.buildclerk

import com.google.inject.Guice
import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.inject.ClerkModule
import io.gatehill.buildclerk.inject.DaemonModule
import io.gatehill.buildclerk.inject.buildStoreModule
import io.gatehill.buildclerk.parser.inject.InstanceFactory
import io.gatehill.buildclerk.parser.inject.InstanceFactoryLocator
import io.gatehill.buildclerk.server.Server
import io.gatehill.buildclerk.util.VersionUtil
import org.apache.logging.log4j.LogManager

/**
 * Main entrypoint.
 */
object Clerk {
    private val logger = LogManager.getLogger("io.gatehill.buildclerk.Clerk")

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Starting Build Clerk, version ${VersionUtil.version}")

        val injector = Guice.createInjector(
            ClerkModule(),
            DaemonModule(),
            buildStoreModule(logger, Settings.Store.implementation)
        )

        InstanceFactoryLocator.instanceFactory = object : InstanceFactory {
            override fun <T : Any> instance(clazz: Class<T>) = injector.getInstance(clazz)
        }

        val server = injector.getInstance(Server::class.java)
        server.startServer()
    }
}
