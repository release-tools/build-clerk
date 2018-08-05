package com.gatehill.buildbouncer.service.runner.jenkins

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.service.runner.BuildRunnerService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Triggers Jenkins jobs and obtains basic job status.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class JenkinsBuildRunnerServiceImpl(
        private val apiClientBuilder: JenkinsApiClientBuilder
) : BuildRunnerService {

    private val logger: Logger = LogManager.getLogger(JenkinsBuildRunnerServiceImpl::class.java)

    override fun rebuild(outcome: BuildOutcome) {
        val jobName = outcome.name

        val apiClient: JenkinsApi
        val call: Call<Void>
        try {
            val headers = mutableMapOf<String, String>()
            obtainCsrfToken()?.let { headers.plusAssign(it) }

            apiClient = apiClientBuilder.buildApiClient(headers)

            call = apiClient.enqueueBuild(
                    token = Settings.Jenkins.apiToken,
                    jobPath = outcome.url
            )
        } catch (e: Exception) {
            throw RuntimeException("Error building API client or obtaining CSRF token", e)
        }

        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, cause: Throwable) {
                logger.error("Error triggering job: $jobName", cause)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    handleTriggerResponse(call, jobName, response)
                } else {
                    logger.error("Unsuccessfully triggered job [ID: $jobName, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
                }
            }
        })
    }

    /**
     * Process the response to triggering a build.
     */
    private fun handleTriggerResponse(call: Call<Void>, jobName: String, response: Response<Void>) {
        when (response.code()) {
            201 -> {
                val queuedItemUrl: String? = response.headers()["Location"]
                if (queuedItemUrl == null) {
                    logger.error("No item was queued for triggered job: $jobName")
                } else {
                    logger.debug("Queued item URL: $queuedItemUrl for job: $jobName")
                }
            }
            else -> {
                logger.error("Unsuccessfully triggered job [ID: $jobName, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }
        }
    }

    /**
     * Obtain CSRF token, if one can be provided by Jenkins.
     * See <a href="https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API#RemoteaccessAPI-CSRFProtection">docs</a>.
     */
    private fun obtainCsrfToken(): Pair<String, String>? {
        var tokenPair: Pair<String, String>? = null

        // this initial API client won't have an CSRF token
        val apiClient = apiClientBuilder.buildApiClient()
        val response = apiClient.fetchCrumb().execute()

        when (response.code()) {
            401 -> throw IllegalStateException("Jenkins authentication failed")
            404 -> logger.debug("CSRF protection is disabled in Jenkins")
            200 -> {
                logger.debug("CSRF token obtained from Jenkins")
                val csrfToken = response.body().string()

                // Jenkins 1.x and 2.x support (see https://issues.jenkins-ci.org/browse/JENKINS-12875)
                arrayOf(".crumb", "Jenkins-Crumb").forEach {
                    if (csrfToken.startsWith("$it:")) tokenPair = Pair(it, csrfToken.substring(it.length + 1))
                }

                tokenPair ?: throw IllegalStateException("Unable to parse CSRF token")
            }
            else -> throw RuntimeException("Jenkins returned HTTP ${response.code()} ${response.message()}")
        }

        return tokenPair
    }
}
