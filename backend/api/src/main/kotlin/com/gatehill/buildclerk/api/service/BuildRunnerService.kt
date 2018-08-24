package com.gatehill.buildclerk.api.service

import com.gatehill.buildclerk.api.model.BuildReport

/**
 * Executes builds.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildRunnerService {
    fun rebuild(report: BuildReport)
}
