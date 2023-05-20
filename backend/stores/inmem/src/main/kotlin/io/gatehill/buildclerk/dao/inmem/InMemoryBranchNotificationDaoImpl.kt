package io.gatehill.buildclerk.dao.inmem

import io.gatehill.buildclerk.api.dao.BranchNotificationDao
import io.gatehill.buildclerk.api.model.BranchNotification
import io.gatehill.buildclerk.dao.inmem.model.Record

/**
 * Stores branch notifications for a user in memory.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class InMemoryBranchNotificationDaoImpl : AbstractInMemoryDao<BranchNotification>(), BranchNotificationDao {
    override val store = mutableListOf<Record<BranchNotification>>()

    override fun addNotification(notification: BranchNotification) {
        store += Record.create(notification.copy(
            branch = notification.branch.lowercase(),
            userId = notification.userId.lowercase()
        ))
    }

    override fun fetchNotificationsForUser(userId: String): List<BranchNotification> {
        return store.filter { it.record.userId == userId.lowercase() }
            .sortedBy { it.createdDate }
            .map { it.record }
    }

    override fun removeNotificationForUser(userId: String, branch: String) {
        store.removeIf {
            it.record.userId == userId.lowercase() && it.record.branch.equals(branch, ignoreCase = true)
        }
    }

    override fun removeAllNotificationsForUser(userId: String) {
        store.removeIf { it.record.userId == userId.lowercase() }
    }

    override fun findMatching(branch: String): List<BranchNotification> {
        return store.filter { branch.contains(it.record.branch, ignoreCase = true) }
            .sortedBy { it.createdDate }
            .map { it.record }
    }
}
