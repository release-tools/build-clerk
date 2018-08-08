package com.gatehill.buildbouncer.api.service

import com.gatehill.buildbouncer.api.model.Analysis

interface NotificationService {
    fun notify(channelName: String, message: String, color: String = "#000000")
    fun notify(channelName: String, analysis: Analysis, color: String = "#000000")
}
