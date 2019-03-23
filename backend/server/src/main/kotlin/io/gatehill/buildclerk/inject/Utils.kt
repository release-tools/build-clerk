package io.gatehill.buildclerk.inject

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.binder.ScopedBindingBuilder
import io.gatehill.buildclerk.api.dao.BranchNotificationDao
import io.gatehill.buildclerk.api.dao.BuildReportDao
import io.gatehill.buildclerk.api.dao.PendingActionDao
import io.gatehill.buildclerk.api.dao.PullRequestEventDao
import io.gatehill.buildclerk.api.dao.Store
import io.gatehill.buildclerk.plugins.PluginLocator
import org.apache.logging.log4j.Logger

/**
 * Syntactic sugar for binding Guice singletons.
 */
internal fun ScopedBindingBuilder.asSingleton() = this.`in`(Singleton::class.java)

/**
 * @return an injection module for store implementations
 */
internal fun buildStoreModule(logger: Logger, implName: String) = object : AbstractModule() {
    override fun configure() {
        logger.debug("Using '$implName' store implementation")
        val store = PluginLocator.findImplementation<Store>(implName)

        bind(BranchNotificationDao::class.java).to(store.branchNotificationDao)
        bind(BuildReportDao::class.java).to(store.buildReportDao)
        bind(PendingActionDao::class.java).to(store.pendingActionDao)
        bind(PullRequestEventDao::class.java).to(store.pullRequestEventDao)
    }
}
