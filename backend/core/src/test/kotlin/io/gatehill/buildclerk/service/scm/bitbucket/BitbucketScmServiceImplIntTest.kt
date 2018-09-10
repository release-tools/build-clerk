package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.service.CommandExecutorService
import io.gatehill.buildclerk.service.support.IntegrationTest
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Integration tests for Bitbucket SCM implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Category(IntegrationTest::class)
class BitbucketScmServiceImplIntTest {
    @Test
    fun lockBranch() {
        val apiClientBuilder = BitbucketApiClientBuilder()

        val service = BitbucketScmServiceImpl(
            Settings.Repository(),
            CommandExecutorService(),
            apiClientBuilder,
            BitbucketOperationsService(apiClientBuilder)
        )
        service.lockBranch("test")
    }
}
