package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.Store
import io.gatehill.buildclerk.plugins.Plugin

/**
 * In memory store implementation.
 *
 * @author pete
 */
@Plugin("inmem")
class InMemoryStore : Store {
    override val buildReportDao = InMemoryBuildReportDaoImpl::class.java
    override val pullRequestEventDao = InMemoryPullRequestEventDaoImpl::class.java
    override val pendingActionDao = InMemoryPendingActionDaoImpl::class.java
}
