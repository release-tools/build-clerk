package com.gatehill.buildbouncer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildOutcome(
    val name: String,
    val build: BuildDetails
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildDetails(
    val number: Int,
    val status: BuildStatus,
    val url: String,
    val scm: Scm
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Scm (
    val branch: String,
    val commit: String
)

enum class BuildStatus {
    SUCCESS,
    FAILED
}
