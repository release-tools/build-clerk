package com.gatehill.buildbouncer.service.runner

import com.gatehill.buildbouncer.model.BuildOutcome

interface BuildRunnerService {
    fun rebuild(outcome: BuildOutcome)
}
