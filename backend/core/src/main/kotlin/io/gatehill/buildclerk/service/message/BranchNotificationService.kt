package io.gatehill.buildclerk.service.message

import io.gatehill.buildclerk.api.dao.BranchNotificationDao
import io.gatehill.buildclerk.api.model.BranchNotification
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class BranchNotificationService @Inject constructor(
    private val branchNotificationDao: BranchNotificationDao
) {
    private val logger = LogManager.getLogger(BranchNotificationService::class.java)

    fun registerNotificationForUser(userId: String, channel: String, branch: String) {
        logger.info("Registering notification for branch: $branch and user: $userId")
        branchNotificationDao.addNotification(
            BranchNotification(
                userId = userId,
                channel = channel,
                branch = branch
            )
        )
    }

    fun unregisterNotificationForUser(userId: String, branch: String) {
        logger.info("Unregistering notification for branch: $branch and user: $userId")
        branchNotificationDao.removeNotificationForUser(userId, branch)
    }

    fun unregisterAllNotificationsForUser(userId: String) {
        logger.info("Removing all branch notifications for user: $userId")
        branchNotificationDao.removeAllNotificationsForUser(userId)
    }

    fun fetchNotificationsForUser(userId: String): List<BranchNotification> {
        val notifications = branchNotificationDao.fetchNotificationsForUser(userId)
        logger.info("${notifications.size} branch notification matches found for user: $userId")
        return notifications
    }

    fun checkForMatches(branch: String): List<BranchNotification> {
        val matching = branchNotificationDao.findMatching(branch)
        if (logger.isDebugEnabled) {
            logger.debug("${matching.size} notification matches found for branch: $branch: $matching")
        } else {
            logger.info("${matching.size} notification matches found for branch: $branch")
        }
        return matching
    }
}
