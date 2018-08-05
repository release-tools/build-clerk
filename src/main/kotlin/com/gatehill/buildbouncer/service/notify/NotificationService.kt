package com.gatehill.buildbouncer.service.notify

import com.gatehill.buildbouncer.model.Analysis

interface NotificationService {
    fun notify(analysis: Analysis)
}
