package com.gatehill.buildbouncer.service.runner.jenkins

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.service.rest.ApiClientBuilder
import okhttp3.Credentials

/**
 * Builds Jenkins REST client.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class JenkinsApiClientBuilder : ApiClientBuilder<JenkinsApi> {
    override val baseUrl: String
        get() = Settings.Jenkins.baseUrl

    override fun buildApiClient(headers: Map<String, String>): JenkinsApi {
        val allHeaders: Map<String, String> = headers.toMutableMap().apply {
            // add HTTP Basic Authorization header if configuration is set
            Settings.Jenkins.username?.let {
                put("Authorization", Credentials.basic(it, Settings.Jenkins.password))
            }
        }

        return buildApiClient(JenkinsApi::class.java, allHeaders)
    }
}
