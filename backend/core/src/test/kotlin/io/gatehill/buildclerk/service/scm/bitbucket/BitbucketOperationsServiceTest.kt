package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.service.support.IntegrationTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Integration tests for Bitbucket SCM implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Category(IntegrationTest::class)
class BitbucketOperationsServiceTest {
    @Test
    fun listComments() {
        val service = buildService()

        val comments = service.listComments(
            pullRequestId = 1
        )

        assertEquals(1, comments.size)
    }

    @Test
    fun createComment() {
        val service = buildService()

        service.createComment(
            pullRequestId = 1,
            comment = "Test comment"
        )
    }

    private fun buildService() = BitbucketOperationsService(BitbucketApiClientBuilder())
}
