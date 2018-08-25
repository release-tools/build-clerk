package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.Store
import io.gatehill.buildclerk.plugins.Plugin

/**
 * MongoDB store implementation.
 *
 * @author pete
 */
@Plugin("mongo")
class MongoStore : Store {
    override val buildReportDao = MongoBuildReportDaoImpl::class.java
    override val pullRequestEventDao = MongoPullRequestEventDaoImpl::class.java
    override val pendingActionDao = MongoPendingActionDaoImpl::class.java
}
