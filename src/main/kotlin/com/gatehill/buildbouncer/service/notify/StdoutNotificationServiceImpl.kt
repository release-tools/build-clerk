package com.gatehill.buildbouncer.service.notify

import com.gatehill.buildbouncer.model.Analysis

class StdoutNotificationServiceImpl : NotificationService {
    override fun notify(analysis: Analysis) {
        println(analysis)
    }
}
