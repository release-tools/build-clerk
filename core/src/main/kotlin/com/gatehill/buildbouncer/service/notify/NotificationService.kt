package com.gatehill.buildbouncer.service.notify

import com.gatehill.buildbouncer.api.model.Analysis

interface NotificationService {
    fun notify(analysis: Analysis)
}
