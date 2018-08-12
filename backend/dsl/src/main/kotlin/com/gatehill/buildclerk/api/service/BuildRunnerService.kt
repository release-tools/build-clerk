package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.BuildOutcome

/**
 * Executes builds.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildRunnerService {
    fun rebuild(outcome: BuildOutcome)
}
