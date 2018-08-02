package com.gatehill.scmwebhook.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BranchStatus(
    val buildNumber: Int,
    val outcome: BuildOutcome
)

enum class BuildOutcome {
    SUCCESS,
    FAILED
}
