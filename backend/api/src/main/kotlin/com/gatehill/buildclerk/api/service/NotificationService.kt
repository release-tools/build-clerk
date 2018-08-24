package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.analysis.Analysis
import com.gatehill.buildclerk.api.model.message.UpdatedNotificationMessage

interface NotificationService {
    fun notify(channelName: String, message: String, color: String = "#000000")
    fun notify(channelName: String, analysis: Analysis, color: String = "#000000")
    fun updateMessage(updatedMessage: UpdatedNotificationMessage)
}
