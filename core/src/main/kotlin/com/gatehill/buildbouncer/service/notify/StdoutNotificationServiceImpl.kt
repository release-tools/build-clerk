package com.gatehill.buildbouncer.service.notify

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.UpdatedNotificationMessage
import com.gatehill.buildbouncer.api.service.NotificationService

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
