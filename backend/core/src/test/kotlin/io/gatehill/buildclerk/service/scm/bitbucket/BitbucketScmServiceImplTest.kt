package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.config.Settings
import io.gatehill.buildclerk.service.CommandExecutorService
import io.gatehill.buildclerk.service.support.IntegrationTest
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Tests for Bitbucket SCM implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Category(IntegrationTest::class)
class BitbucketScmServiceImplTest {
    @Test
    fun lockBranch() {
        val service = BitbucketScmServiceImpl(
            Settings.Repository(),
            CommandExecutorService(),
            BitbucketApiClientBuilder(),
            BitbucketOperationsService()
        )
        service.lockBranch("test")
    }
}
