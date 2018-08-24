package com.gatehill.buildclerk.service.notify

import com.gatehill.buildclerk.api.model.analysis.Analysis
import com.gatehill.buildclerk.api.model.message.UpdatedNotificationMessage
import com.gatehill.buildclerk.api.service.NotificationService

open class StdoutNotificationServiceImpl : NotificationService {
    override fun notify(channelName: String, message: String, color: String) {
        println("$channelName: $message")
    }

    override fun notify(channelName: String, analysis: Analysis, color: String) {
        notify(channelName, analysis.toString())
    }

    override fun updateMessage(updatedMessage: UpdatedNotificationMessage) {
        println(updatedMessage)
    }
}
