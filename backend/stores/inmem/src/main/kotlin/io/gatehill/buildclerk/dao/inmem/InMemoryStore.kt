package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.Store
import io.gatehill.buildclerk.plugins.Plugin

/**
 * In memory store implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Plugin("inmem")
class InMemoryStore : Store {
    override val branchNotificationDao = InMemoryBranchNotificationDaoImpl::class.java
    override val buildReportDao = InMemoryBuildReportDaoImpl::class.java
    override val pendingActionDao = InMemoryPendingActionDaoImpl::class.java
    override val pullRequestEventDao = InMemoryPullRequestEventDaoImpl::class.java
}
