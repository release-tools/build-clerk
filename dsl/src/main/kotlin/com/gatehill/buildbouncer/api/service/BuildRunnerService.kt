package com.gatehill.buildbouncer.api.service

import com.gatehill.buildbouncer.api.model.BuildOutcome

/**
 * Executes builds.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildRunnerService {
    fun rebuild(outcome: BuildOutcome)
}
