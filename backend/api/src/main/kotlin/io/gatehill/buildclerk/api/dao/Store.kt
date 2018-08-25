package io.gatehill.buildclerk.api.dao

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface Store {
    val buildReportDao: Class<out BuildReportDao>
    val pullRequestEventDao: Class<out PullRequestEventDao>
    val pendingActionDao: Class<out PendingActionDao>
}
