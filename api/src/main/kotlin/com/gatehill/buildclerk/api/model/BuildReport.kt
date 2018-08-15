package com.gatehill.buildclerk.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildReport(
        /**
         * Example: `asgard`
         */
        val name: String,

        /**
         * Example: `job/asgard/`
         */
        val url: String,

        val build: BuildDetails
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildDetails(
        val number: Int,
        val status: BuildStatus,
        val scm: Scm,

        @JsonProperty("full_url")
        val fullUrl: String,

        /**
         * User or other cause.
         * Only provided by Clerk plugin.
         */
        val triggeredBy: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Scm(
        val branch: String,
        val commit: String
)

enum class BuildStatus {
    SUCCESS,
    FAILED,

    /**
     * Insufficient data.
     */
    UNKNOWN
}
