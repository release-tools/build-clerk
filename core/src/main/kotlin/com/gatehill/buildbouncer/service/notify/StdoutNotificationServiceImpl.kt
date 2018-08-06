package com.gatehill.buildbouncer.service.notify

import com.gatehill.buildbouncer.api.model.Analysis

open class StdoutNotificationServiceImpl : NotificationService {
    override fun notify(analysis: Analysis) {
        println(analysis)
    }
}
