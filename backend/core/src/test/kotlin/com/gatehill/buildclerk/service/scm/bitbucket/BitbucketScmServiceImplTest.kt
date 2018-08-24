package com.gatehill.buildclerk.service.scm.bitbucket

import com.gatehill.buildclerk.service.CommandExecutorService
import com.gatehill.buildclerk.service.support.IntegrationTest
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
            CommandExecutorService(),
            BitbucketApiClientBuilder(),
            BitbucketOperationsService()
        )
        service.lockBranch("test")
    }
}
