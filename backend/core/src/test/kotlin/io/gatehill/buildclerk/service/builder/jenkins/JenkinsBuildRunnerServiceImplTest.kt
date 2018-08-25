package io.gatehill.buildclerk.service.builder.jenkins

import com.nhaarman.mockitokotlin2.mock
import io.gatehill.buildclerk.api.model.BuildDetails
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.Scm
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests for `JenkinsBuildRunnerServiceImpl`.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class JenkinsBuildRunnerServiceImplTest {
    private lateinit var service: JenkinsBuildRunnerServiceImpl

    @Before
    fun setUp() {
        val apiClientBuilder = mock<JenkinsApiClientBuilder>()
        service = JenkinsBuildRunnerServiceImpl(apiClientBuilder)
    }

    /**
     * This can occur if the Jenkins URL is not set in Jenkins' configuration.
     */
    @Test
    fun `calculate job path for relative job URL`() {
        val report = buildReport(
            shortUrl = "job/example/",
            fullUrl = "job/example/11/"
        )

        val jobPath = service.calculateJobPath(report)
        assertEquals("job/example", jobPath)
    }

    @Test
    fun `calculate job path for simple job URL`() {
        val report = buildReport(
            shortUrl = "job/example/",
            fullUrl = "https://jenkins.example.com/job/example/11/"
        )

        val jobPath = service.calculateJobPath(report)
        assertEquals("job/example", jobPath)
    }

    @Test
    fun `calculate job path for multibranch job URL`() {
        val report = buildReport(
            shortUrl = "job/example/",
            fullUrl = "https://jenkins.example.com/job/example/job/some-branch/11/"
        )

        val jobPath = service.calculateJobPath(report)
        assertEquals("job/example/job/some-branch", jobPath)
    }

    private fun buildReport(shortUrl: String, fullUrl: String): BuildReport {
        return BuildReport(
            name = "example",
            url = shortUrl,
            build = BuildDetails(
                number = 1,
                status = BuildStatus.SUCCESS,
                fullUrl = fullUrl,
                scm = Scm(
                    branch = "master",
                    commit = "c0ff33"
                )
            )
        )
    }
}
