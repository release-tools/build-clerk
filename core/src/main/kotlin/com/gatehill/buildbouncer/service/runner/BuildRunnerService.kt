package com.gatehill.buildbouncer.service.runner

import com.gatehill.buildbouncer.model.BuildOutcome

/**
 * Executes builds.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildRunnerService {
    fun rebuild(outcome: BuildOutcome)
}
