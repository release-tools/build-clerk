package io.gatehill.buildclerk.api.dao

import io.gatehill.buildclerk.api.model.BranchNotification

/**
 * Stores user branch notification requests.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BranchNotificationDao {
    fun addNotification(notification: BranchNotification)

    fun fetchNotificationsForUser(userId: String): List<BranchNotification>

    fun removeNotificationForUser(userId: String, branch: String)

    fun removeAllNotificationsForUser(userId: String)

    fun findMatching(branch: String): List<BranchNotification>
}
