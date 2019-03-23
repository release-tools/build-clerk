package io.gatehill.buildclerk.dao.mongo

import io.gatehill.buildclerk.api.dao.BranchNotificationDao
import io.gatehill.buildclerk.api.model.BranchNotification
import io.gatehill.buildclerk.dao.mongo.model.MongoBranchNotificationWrapper
import io.gatehill.buildclerk.dao.mongo.model.wrap
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.div
import org.litote.kmongo.eq

/**
 * Stores branch notifications for a user in MongoDB.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class MongoBranchNotificationDaoImpl : AbstractMongoDao(), BranchNotificationDao {
    override val collectionName = "branch_notifications"

    override fun addNotification(notification: BranchNotification) =
        withCollection<MongoBranchNotificationWrapper, Unit> {
            insertOne(
                notification.copy(
                    branch = notification.branch.toLowerCase()
                ).wrap()
            )
        }

    override fun fetchNotificationsForUser(userId: String): List<BranchNotification> =
        withCollection<MongoBranchNotificationWrapper, List<BranchNotification>> {
            find(MongoBranchNotificationWrapper::notification / BranchNotification::userId eq userId)
                .sort(ascending(MongoBranchNotificationWrapper::createdDate))
                .map { it.notification }
                .toList()
        }

    override fun removeNotificationForUser(userId: String, branch: String) =
        withCollection<MongoBranchNotificationWrapper, Unit> {
            deleteOne(
                and(
                    MongoBranchNotificationWrapper::notification / BranchNotification::userId eq userId,
                    MongoBranchNotificationWrapper::notification / BranchNotification::branch eq branch.toLowerCase()
                )
            )
        }

    override fun removeAllNotificationsForUser(userId: String) =
        withCollection<MongoBranchNotificationWrapper, Unit> {
            deleteMany(MongoBranchNotificationWrapper::notification / BranchNotification::userId eq userId)
        }

    override fun findMatching(branch: String): List<BranchNotification> =
        withCollection<MongoBranchNotificationWrapper, List<BranchNotification>> {
            find(MongoBranchNotificationWrapper::notification / BranchNotification::branch eq branch.toLowerCase())
                .sort(ascending(MongoBranchNotificationWrapper::createdDate))
                .map { it.notification }
                .toList()
        }
}
